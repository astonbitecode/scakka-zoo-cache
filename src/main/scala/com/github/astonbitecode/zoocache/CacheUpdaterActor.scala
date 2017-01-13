package com.github.astonbitecode.zoocache

import akka.actor.{ Actor, Props }
import scala.collection.mutable.{ Map, HashSet }
import com.github.astonbitecode.zoocache.messages._
import org.apache.zookeeper.{
  ZooKeeper,
  Watcher,
  WatchedEvent,
  KeeperException
}
import scala.concurrent.Promise
import org.apache.zookeeper.Watcher.Event
import scala.collection.JavaConversions._
import scala.util.{
  Try,
  Success,
  Failure
}
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.Logger
import org.apache.zookeeper.KeeperException.NoNodeException
import com.github.astonbitecode.zoocache.CacheUpdaterActor.ZkNodeElement
import com.github.astonbitecode.zoocache.zk.ZookeeperManager

object CacheUpdaterActor {
  def props(cache: Map[String, ZkNodeElement], zoo: ZookeeperManager): Props = {
    Props(new CacheUpdaterActor(cache, zoo))
  }

  private[astonbitecode] case class ZkNodeElement(data: Array[Byte], children: Set[String] = Set.empty)
}

private class CacheUpdaterActor(cache: Map[String, ZkNodeElement], zoo: ZookeeperManager) extends Actor {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  // Add the watcher
  zoo.registerWatcher(new ZooKeeperWatcher)
  // Keep a set of watched nodes in order not to add more watches that we should
  val watchedNodes = new HashSet[String]
  // Keep a set of Added Paths
  val addedPaths = new HashSet[String]

  override def receive(): Receive = {
    case wp @ ScakkaApiWatchUnderPath(path, _) => {
      // The added paths should be updated only after successful invocation.
      // This is why a Decorator is implemented
      def onSuccessDecoratedAction(): Unit = {
        addedPaths.add(path)
      }
      // Create and send the decorator
      val wpDecorator = NotifyableDecorator(wp, onSuccessDecoratedAction)
      self ! Update(path, true, Some(wpDecorator))
    }
    case rp @ ScakkaApiRemovePath(path, _) => {
      // The added paths should be updated only after successful invocation.
      // This is why a Decorator is implemented
      def onSuccessDecoratedAction(): Unit = {
        addedPaths.remove(path)
      }
      // Create and send the decorator
      val rpDecorator = NotifyableDecorator(rp, onSuccessDecoratedAction)
      self ! Remove(path, Some(rpDecorator))
    }
    case ScakkaApiShutdown => {
      logger.info("Shutting down scakka-zoo-cache")
      addedPaths.clear
      watchedNodes.clear
      cache.clear
      context.stop(self)
    }
    // Add a path entry to the cache
    case Add(path, data, updateChildren, notifOpt) => {
      setWatchers(path)
      Try(zoo.getChildren(path)) match {
        case Success(children) => {
          cache.put(path, ZkNodeElement(data, children))
          if (updateChildren) {
            children.foreach { child => self ! Update(s"$path/$child", updateChildren, notifOpt) }
          }
          succeedNotifyable(notifOpt, path)
        }
        case Failure(error: NoNodeException) => {
          failNotifyable(notifOpt, error, path)
          self ! Remove(path, None)
        }
        case Failure(error) => {
          logger.debug(s"Could not get children of $path while adding", error)
          failNotifyable(notifOpt, error, path)
        }
      }
    }
    // Update a path entry from the ZooKeeper
    case Update(path, recursive, notifOpt) => {
      Try(zoo.getData(path)) match {
        case Success(data) => self ! Add(path, data, recursive, notifOpt)
        case Failure(error: KeeperException.NoNodeException) => self ! SetWatcher(path, notifOpt)
        case Failure(error) => logger.debug(s"Could not get data of $path while updating", error)
      }
    }
    // Remove a path from the cache. Called either when the path was deleted from the ZooKeeper,
    // or when a API user does not need the path to be cached
    case Remove(path, notifOpt) => {
      watchedNodes.remove(path)
      // If the path to remove is one of the paths that the user has defined, set watchers in order to get the data when they appear
      if (addedPaths.contains(path)) {
        setWatchers(path)
      }
      cache.remove(path) match {
        case Some(zkne) => {
          succeedNotifyable(notifOpt, path)
          zkne.children.foreach { child => self ! Remove(s"$path/$child", notifOpt) }
        }
        case None => // ignore
      }
    }
    // Set a watcher
    case SetWatcher(path, _) => setWatchers(path)
    // Remove watch from a path
    case Unwatch(path, _) => watchedNodes.remove(path)
    case other: Any => logger.debug(s"${this.getClass} cannot handle $other of type (${other.getClass})")
  }

  def setWatchers(path: String): Unit = {
    if (!watchedNodes.contains(path)) {
      Try {
        zoo.setWatchers(path)
      } match {
        case Success(_) => watchedNodes.add(path)
        case Failure(error: NoNodeException) => self ! Remove(path, None)
        case Failure(error) => logger.debug(s"Could not add watchers for $path", error)
      }
    }
  }

  class ZooKeeperWatcher extends Watcher {
    def process(event: WatchedEvent): Unit = {
      logger.debug(s"Processing event type '${event.getType.name}' for path '${event.getPath}'")
      if (event.getPath != null && event.getType != null && watchedNodes.contains(event.getPath)) {
        self ! Unwatch(event.getPath, None)

        event.getType match {
          case Event.EventType.None => {
            // ignore
          }
          case Event.EventType.NodeCreated => {
            self ! Update(event.getPath, true, None)
          }
          case Event.EventType.NodeDeleted => {
            self ! Remove(event.getPath, None)
          }
          case Event.EventType.NodeDataChanged => {
            self ! Update(event.getPath, false, None)
          }
          case Event.EventType.NodeChildrenChanged => {
            self ! Update(event.getPath, true, None)
          }
        }
      }
    }
  }

  def succeedNotifyable(notifyableOpt: Option[MessageNotifyable], path: String): Unit = {
    notifyableOpt match {
      case Some(notif) => notif.success(path)
      case other => //ignore
    }
  }

  def failNotifyable(notifyableOpt: Option[MessageNotifyable], error: Throwable, path: String): Unit = {
    notifyableOpt match {
      case Some(notif) => notif.failure(path, error)
      case other => //ignore
    }
  }

  /**
   * Decorates a MessageNotifyable in order to add specific functionality that only the CacheUpdaterActor knows
   * @param notifyable: The MEssageNotifyable to decorate
   * @param additionalActionOnSuccess A function () => Unit to be executed upon success
   */
  private[zoocache] case class NotifyableDecorator(notifyable: MessageNotifyable,
      additionalActionOnSuccess: () => Unit) extends MessageNotifyable {

    def success(path: String): Boolean = {
      val b = notifyable.success(path)
      if (b) {
        additionalActionOnSuccess()
      }
      b
    }

    def failure(path: String, error: Throwable): Boolean = {
      notifyable.failure(path, error)
    }
  }
}