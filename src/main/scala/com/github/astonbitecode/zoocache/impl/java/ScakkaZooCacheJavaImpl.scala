package com.github.astonbitecode.zoocache.impl.java

import com.github.astonbitecode.zoocache.api.java.JScakkaZooCache
import com.github.astonbitecode.zoocache.api.ScakkaException.NotCachedException
import scala.concurrent.{ Future, Await }
import scala.concurrent.duration._
import com.github.astonbitecode.zoocache.api.scala.ScakkaZooCache
import scala.collection.JavaConversions._
import java.lang.String
import java.util.List
import com.github.astonbitecode.zoocache.api.dtos.CacheResult

class ScakkaZooCacheJavaImpl(scakkaCache: ScakkaZooCache) extends JScakkaZooCache {

  @throws(classOf[NotCachedException])
  def getChildren(path: String): List[String] = scakkaCache.getChildren(path)

  @throws(classOf[NotCachedException])
  def getData(path: String): Array[Byte] = scakkaCache.getData(path)

  def find(regex: String): List[CacheResult] = scakkaCache.find(regex)

  def addPathToCache(path: String): java.util.concurrent.Future[Unit] = scakkaCache.addPathToCache(path)

  def removePathFromCache(path: String): java.util.concurrent.Future[Unit] = scakkaCache.removePathFromCache(path)

  def stop(): java.util.concurrent.Future[Unit] = scakkaCache.stop

  private implicit def scalaFuture2JavaFuture[T](x: Future[T]): java.util.concurrent.Future[T] = {
    new java.util.concurrent.Future[T] {

      override def isCancelled: Boolean = throw new UnsupportedOperationException

      override def get(): T = Await.result(x, 30.minutes)

      override def get(timeout: Long, unit: TimeUnit): T = Await.result(x, Duration.create(timeout, unit))

      override def cancel(mayInterruptIfRunning: Boolean): Boolean = throw new UnsupportedOperationException

      override def isDone: Boolean = x.isCompleted
    }
  }
}