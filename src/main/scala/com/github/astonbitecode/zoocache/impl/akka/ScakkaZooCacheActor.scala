package com.github.astonbitecode.zoocache.impl.akka

import com.github.astonbitecode.zoocache.api.scala.ScakkaZooCache
import com.github.astonbitecode.zoocache.api.akka._
import akka.actor.{ Props, Actor }
import org.slf4j.LoggerFactory
import scala.util.{
  Try,
  Success,
  Failure
}
import com.typesafe.scalalogging.Logger

object ScakkaZooCacheActor {
  def props(zooCache: ScakkaZooCache): Props = {
    Props(new ScakkaZooCacheActor(zooCache))
  }
}

class ScakkaZooCacheActor(zooCache: ScakkaZooCache) extends Actor {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  implicit val ec = context.dispatcher

  override def receive(): Receive = {
    case GetChildren(path, correlation) => {
      Try(zooCache.getChildren(path)) match {
        case Success(children) => sender ! GetChildrenResponse(path, children, correlation)
        case Failure(error) => sender ! CacheFailure(error, correlation)
      }
    }
    case GetData(path, correlation) => {
      Try(zooCache.getData(path)) match {
        case Success(data) => sender ! GetDataResponse(path, data, correlation)
        case Failure(error) => sender ! CacheFailure(error, correlation)
      }
    }
    case AddPathToCache(path, correlation) => {
      val replyTo = sender
      zooCache.addPathToCache(path).andThen {
        case Success(_) => replyTo ! AddPathToCacheResponse(path, correlation)
        case Failure(error) => replyTo ! CacheFailure(error, correlation)
      }
    }
    case RemovePathFromCache(path, correlation) => {
      val replyTo = sender
      zooCache.removePathFromCache(path).andThen {
        case Success(_) => replyTo ! RemovePathFromCacheResponse(path, correlation)
        case Failure(error) => replyTo ! CacheFailure(error, correlation)
      }
    }
    case Stop(correlation) => {
      val replyTo = sender
      zooCache.stop().andThen {
        case Success(_) => replyTo ! StopResponse(correlation)
        case Failure(error) => replyTo ! CacheFailure(error, correlation)
      }
    }
    case other: Any => logger.debug(s"${this.getClass} cannot handle $other of type (${other.getClass})")
  }
}