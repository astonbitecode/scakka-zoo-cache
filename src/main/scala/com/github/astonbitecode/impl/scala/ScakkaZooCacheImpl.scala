package com.github.astonbitecode.impl.scala

import scala.collection.mutable.HashMap
import org.apache.zookeeper.{ ZooKeeper, KeeperException }
import org.apache.zookeeper.KeeperException.Code
import akka.actor.ActorSystem
import scala.concurrent.{ Future, Promise }
import com.github.astonbitecode.messages._
import akka.actor.actorRef2Scala
import com.github.astonbitecode.CacheUpdaterActor
import org.apache.zookeeper.KeeperException
import com.github.astonbitecode.api.scala.ScakkaZooCache

case class ScakkaZooCacheImpl(zoo: ZooKeeper, actorSystem: ActorSystem) extends ScakkaZooCache {
  // Import from companion
  import ScakkaZooCache.ZkNodeElement
  // The cache
  private[astonbitecode] val cache = HashMap.empty[String, ZkNodeElement]
  // Create only one handler.
  // WARNING: The handler is the only entity that mutates the cache.
  private val updater = actorSystem.actorOf(CacheUpdaterActor.props(cache, zoo))

  @throws(classOf[KeeperException])
  override def getChildren(path: String): List[String] = {
    cache.get(path).fold(throw KeeperException.create(Code.NONODE))(_.children.toList)
  }

  @throws(classOf[KeeperException])
  override def getData(path: String): Array[Byte] = {
    cache.get(path).fold(throw KeeperException.create(Code.NONODE))(_.data)
  }

  override def addPathToCache(path: String): Future[Unit] = {
    val p = Promise[Unit]

    cache.get(path) match {
      case Some(_) => updater ! ScakkaApiWatchUnderPath(path, Some(p))
      case None => {
        updater ! ScakkaApiWatchUnderPath(path, None)
        p.success()
      }
    }

    p.future
  }

  override def removePathFromCache(path: String): Future[Unit] = {
    val p = Promise[Unit]

    cache.get(path) match {
      case Some(_) => updater ! ScakkaApiWatchUnderPath(path, Some(p))
      case None => {
        updater ! ScakkaApiWatchUnderPath(path, None)
        p.success()
      }
    }

    p.future
  }
}