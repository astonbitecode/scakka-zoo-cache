package com.github.astonbitecode.zoocache.impl.scala

import scala.concurrent.{ Future, Promise }
import scala.concurrent.duration._
import akka.actor.actorRef2Scala
import akka.pattern.gracefulStop
import com.github.astonbitecode.zoocache.messages._
import com.github.astonbitecode.zoocache.CacheUpdaterActor
import com.github.astonbitecode.zoocache.api.scala.ScakkaZooCache
import com.github.astonbitecode.zoocache.api.ScakkaException.NotCachedException
import com.github.astonbitecode.zoocache.Internals.ActorCreatable
import com.github.astonbitecode.zoocache.zk.ZookeeperManager
import com.github.astonbitecode.zoocache.api.dtos.CacheResult
import scala.util.matching.Regex
import scala.collection.concurrent.TrieMap

case class ScakkaZooCacheImpl(zoo: ZookeeperManager, actorCreatable: ActorCreatable) extends ScakkaZooCache {
  // The cache
  private[astonbitecode] val cache = TrieMap.empty[String, CacheUpdaterActor.ZkNodeElement]
  // Create only one handler.
  // WARNING: The handler is the only entity that mutates the cache.
  private val updater = actorCreatable.actorOf(CacheUpdaterActor.props(cache, zoo))

  @throws(classOf[NotCachedException])
  override def getChildren(path: String): List[String] = {
    cache.get(path).fold(throw new NotCachedException(s"Path '$path' is not found in the cache while getting children"))(_.children.toList)
  }

  @throws(classOf[NotCachedException])
  override def getData(path: String): Array[Byte] = {
    cache.get(path).fold(throw new NotCachedException(s"Path '$path' is not found in the cache while getting data"))(_.data)
  }

  override def find(regex: String): List[CacheResult] = {
    val pattern = new Regex(regex)
    cache.filter { kv =>
      {
        pattern.findFirstIn(kv._1).nonEmpty
      }
    }.map { kv =>
      {
        val (path, CacheUpdaterActor.ZkNodeElement(data, children)) = kv
        CacheResult(path, data, children.toList)
      }
    }.toList
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
      case Some(_) => updater ! ScakkaApiRemovePath(path, p)
      case None => p.success()
    }

    p.future
  }

  override def stop(): Future[Unit] = {
    implicit val ec = actorCreatable.dispatcher
    gracefulStop(updater, 10.seconds, ScakkaApiShutdown).map {_ => }
  }
}