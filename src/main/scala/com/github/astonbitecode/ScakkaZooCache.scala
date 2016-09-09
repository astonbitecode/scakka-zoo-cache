package com.github.astonbitecode

import scala.collection.mutable.HashMap
import org.apache.zookeeper.{ ZooKeeper, KeeperException }
import org.apache.zookeeper.KeeperException.Code
import akka.actor.ActorSystem
import scala.concurrent.{ Future, Promise }
import com.github.astonbitecode.messages._

case class ScakkaZooCache(zoo: ZooKeeper, actorSystem: ActorSystem) {
  // Import from companion
  import ScakkaZooCache.ZkNodeElement
  // The cache
  private[astonbitecode] val cache = HashMap.empty[String, ZkNodeElement]
  // Create only one handler.
  // WARNING: The handler is the only entity that mutates the cache.
  private val updater = actorSystem.actorOf(CacheUpdaterActor.props(cache, zoo))

  /**
   * Gets the children of the node of the given path
   */
  @throws(classOf[KeeperException])
  def getChildren(path: String): List[String] = {
    cache.get(path).fold(throw KeeperException.create(Code.NONODE))(_.children.toList)
  }

  /**
   * Gets the data of the node of the given path
   */
  @throws(classOf[KeeperException])
  def getData(path: String): Array[Byte] = {
    cache.get(path).fold(throw KeeperException.create(Code.NONODE))(_.data)
  }

  /**
   * Adds a path to the cache. The cache will be updating all the subtree under the defined path.
   */
  def addPathToCache(path: String): Future[Unit] = {
    val p = Promise[Unit]
    updater ! ScakkaApiWatchUnderPath(path, Some(p))
    p.future
  }
}

object ScakkaZooCache {
  private[astonbitecode] case class ZkNodeElement(data: Array[Byte], children: Set[String] = Set.empty)

  def apply(zoo: ZooKeeper): ScakkaZooCache = {
    val actorSystem = ActorSystem("ScakkaZooCache")
    new ScakkaZooCache(zoo, actorSystem)
  }
}