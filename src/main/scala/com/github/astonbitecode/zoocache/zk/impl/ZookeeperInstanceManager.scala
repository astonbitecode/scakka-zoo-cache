package com.github.astonbitecode.zoocache.zk.impl

import scala.collection.JavaConversions.asScalaBuffer

import org.apache.zookeeper.Watcher
import org.apache.zookeeper.ZooKeeper
import org.slf4j.LoggerFactory

import com.github.astonbitecode.zoocache.zk.ZookeeperManager
import com.typesafe.scalalogging.Logger

class ZookeeperInstanceManager(zoo: ZooKeeper) extends ZookeeperManager {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))

  override def registerWatcher(watcher: Watcher): Unit = {
    // TODO: Can we avoid registering the Watcher like this? Can we use the ZooKeeper methods to register the Watcher?
    zoo.register(watcher)
  }

  override def setWatchers(path: String): Unit = {
    val stat = Option(zoo.exists(path, true))
    if (stat.nonEmpty) {
      zoo.getChildren(path, true)
      zoo.getData(path, true, stat.get)
    }
  }

  override def getChildren(path: String): Set[String] = {
    zoo.getChildren(path, false).toSet
  }

  override def getData(path: String): Array[Byte] = {
    zoo.getData(path, false, null)
  }

}