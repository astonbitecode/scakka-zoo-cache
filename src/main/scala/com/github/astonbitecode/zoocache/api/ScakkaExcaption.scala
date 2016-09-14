package com.github.astonbitecode.zoocache.api

/**
 * General exception thrown by the scakka-zoo-cache.
 * There are more specific exceptions that are all derived from this one.
 */
class ScakkaExcaption(message: String) extends Exception(message) {
  def this() = this("")
}

object ScakkaException {
  /**
   * Thrown when the cache does not contain a specific path.
   * There are two possible reasons why the cache does not contain a path
   * - The cache is not instructed to cache the specific path or one of its parents
   * - Even if the path is added in the cache, it does not exist in the ZooKeeper
   */
  class NotCachedException(message: String) extends ScakkaExcaption(message)
}