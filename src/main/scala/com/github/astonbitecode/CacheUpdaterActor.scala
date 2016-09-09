package com.github.astonbitecode

import akka.actor.{ Actor, Props }
import scala.collection.mutable.{ Map, HashSet }
import com.github.astonbitecode.ScakkaZooCache.ZkNodeElement
import com.github.astonbitecode.messages._
import org.apache.zookeeper.{
  ZooKeeper,
  Watcher,
  WatchedEvent
}
import scala.concurrent.Promise
import org.apache.zookeeper.Watcher.Event
import scala.collection.JavaConversions._

object CacheUpdaterActor {
  def props(cache: Map[String, ZkNodeElement], zoo: ZooKeeper): Props = {
    Props(new CacheUpdaterActor(cache, zoo))
  }

}

private class CacheUpdaterActor(cache: Map[String, ZkNodeElement], zoo: ZooKeeper) extends Actor {
  // Keep a set of watched nodes in order not to add more watches that we should
  val watchedNodes = new HashSet[String]

  override def receive(): Receive = {
    case wp @ ScakkaApiWatchUnderPath(path, promiseOpt) => {
      self ! Update(path, true)
      wp.success()
    }
    // Add a path entry to the cache
    case Add(path, data, updateChildren) => {
      // TODO: Improvement, setWatcher and getChildren in one call
      setWatcher(path)
      val children = zoo.getChildren(path, false).toSet
      cache.put(path, ZkNodeElement(data, children))
      if (updateChildren) {
        children.foreach { child => self ! Update(s"$path/$child", updateChildren) }
      }
    }
    case Update(path, recursive) => {
      val data = zoo.getData(path, false, null)
      self ! (Add(path, data, recursive))
    }
    case _ => // Ignore
  }

  def setWatcher(path: String): Unit = {
    if (!watchedNodes.contains(path)) {

      //      vds.setChildrenNotification(path, watchHandler)
      //      vds.setExistsNotification(path, watchHandler)
      watchedNodes.add(path)
    }
  }

  class ZooKeeperWatcher extends Watcher {
    def process(event: WatchedEvent): Unit = {

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