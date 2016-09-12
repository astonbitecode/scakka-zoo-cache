package com.github.astonbitecode

import akka.actor.{ Actor, Props }
import scala.collection.mutable.{ Map, HashSet }
import com.github.astonbitecode.api.scala.ScakkaZooCache.ZkNodeElement
import com.github.astonbitecode.messages._
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

object CacheUpdaterActor {
  def props(cache: Map[String, ZkNodeElement], zoo: ZooKeeper): Props = {
    Props(new CacheUpdaterActor(cache, zoo))
  }

}

private class CacheUpdaterActor(cache: Map[String, ZkNodeElement], zoo: ZooKeeper) extends Actor {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  // Add the watcher
  // TODO: Can we avoid registering the Watcher like this? Can we use the ZooKeeper methods to register the Watcher?
  zoo.register(new ZooKeeperWatcher)
  // Keep a set of watched nodes in order not to add more watches that we should
  val watchedNodes = new HashSet[String]

  override def receive(): Receive = {
    case wp @ ScakkaApiWatchUnderPath(path, promiseOpt) => {
      self ! Update(path, true)
      wp.success()
    }
    // Add a path entry to the cache
    case Add(path, data, updateChildren) => {
      setWatchers(path)
      Try(zoo.getChildren(path, false).toSet) match {
        case Success(children) => {
          cache.put(path, ZkNodeElement(data, children))
          if (updateChildren) {
            children.foreach { child => self ! Update(s"$path/$child", updateChildren) }
          }
        }
        case Failure(error) => logger.error(s"Could not get children of $path while adding", error)
      }
    }
    // Update a path entry from the ZooKeeper
    case Update(path, recursive) => {
      Try(zoo.getData(path, false, null)) match {
        case Success(data) => self ! Add(path, data, recursive)
        case Failure(error: KeeperException.NoNodeException) => self ! SetWatcher(path)
        case Failure(error) => logger.error(s"Could not get data of $path while updating", error)
      }
    }
    // Set a watcher
    case SetWatcher(path) => setWatchers(path)
    // Remove watch from a path
    case Unwatch(path) => watchedNodes.remove(path)
    case other: Any => logger.error(s"Cannot handle $other of type (${other.getClass})")
  }

  def setWatchers(path: String): Unit = {
    if (!watchedNodes.contains(path)) {
      Try {
        val stat = Option(zoo.exists(path, true))
        if (stat.nonEmpty) {
          zoo.getChildren(path, true)
          zoo.getData(path, true, stat.get)
        }
      } match {
        case Success(_) => watchedNodes.add(path)
        case Failure(error) => logger.error(s"Could not add watchers for $path", error)
      }
    }
  }

  class ZooKeeperWatcher extends Watcher {
    def process(event: WatchedEvent): Unit = {
      if (event.getPath != null && event.getType != null) {
        self ! Unwatch(event.getPath)

        event.getType match {
          case Event.EventType.None => {
            // ignore
          }
          case Event.EventType.NodeCreated => {
            self ! Update(event.getPath, true)
          }
          case Event.EventType.NodeDeleted => {
            self ! Remove(event.getPath)
          }
          case Event.EventType.NodeDataChanged => {
            self ! Update(event.getPath, false)
          }
          case Event.EventType.NodeChildrenChanged => {
            self ! Update(event.getPath, true)
          }
        }
      }
    }
  }
}