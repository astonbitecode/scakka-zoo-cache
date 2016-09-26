package com.github.astonbitecode.zoocache.api.java

import com.github.astonbitecode.zoocache.api.ScakkaException.NotCachedException
import java.util.concurrent.Future
import java.util.List
import java.lang.String

trait JScakkaZooCache {
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
