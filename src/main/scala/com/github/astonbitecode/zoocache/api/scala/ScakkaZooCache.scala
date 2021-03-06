package com.github.astonbitecode.zoocache.api.scala

import scala.concurrent.Future
import org.apache.zookeeper.ZooKeeper
import akka.actor.ActorSystem
import com.github.astonbitecode.zoocache.impl.scala.ScakkaZooCacheImpl
import com.github.astonbitecode.zoocache.api.ScakkaException.NotCachedException
import com.github.astonbitecode.zoocache.api.dtos.CacheResult

trait ScakkaZooCache {
  /**
   * Gets the children of the node of the given path
   */
  @throws(classOf[NotCachedException])
  def getChildren(path: String): List[String]

  /**
   * Gets the data of the node of the given path
   */
  @throws(classOf[NotCachedException])
  def getData(path: String): Array[Byte]

  /**
   * Retrieves the data and children of the paths that match the specified regex.
   * @param: regex The Regular expression to be used in order to find paths in the cache.
   * @returns A list of CacheResult instances.
   */
  def find(regex: String): List[CacheResult]

  /**
   * Adds a path to the cache. The cache will be updating all the subtree under the defined path.
   */
  def addPathToCache(path: String): Future[Unit]

  /**
   * Removes a path from the cache. All the paths of the subtree under the defined path will also be removed from the cache and will stop being updated.
   */
  def removePathFromCache(path: String): Future[Unit]

  /**
   * Stops and shuts down the cache resources
   */
  def stop(): Future[Unit]
}