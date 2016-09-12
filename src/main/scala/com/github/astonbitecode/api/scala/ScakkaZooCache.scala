package com.github.astonbitecode.api.scala

import org.apache.zookeeper.KeeperException
import scala.concurrent.Future
import org.apache.zookeeper.ZooKeeper
import akka.actor.ActorSystem
import com.github.astonbitecode.impl.scala.ScakkaZooCacheImpl

trait ScakkaZooCache {
  /**
   * Gets the children of the node of the given path
   */
  @throws(classOf[KeeperException])
  def getChildren(path: String): List[String]

  /**
   * Gets the data of the node of the given path
   */
  @throws(classOf[KeeperException])
  def getData(path: String): Array[Byte]

  /**
   * Adds a path to the cache. The cache will be updating all the subtree under the defined path.
   */
  def addPathToCache(path: String): Future[Unit]
}

object ScakkaZooCache {
  private[astonbitecode] case class ZkNodeElement(data: Array[Byte], children: Set[String] = Set.empty)

  def scala(zoo: ZooKeeper): ScakkaZooCache = {
    val actorSystem = ActorSystem("ScakkaZooCache")
    new ScakkaZooCacheImpl(zoo, actorSystem)
  }
}