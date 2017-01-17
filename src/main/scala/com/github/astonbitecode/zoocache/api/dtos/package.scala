package com.github.astonbitecode.zoocache.api

/**
 * Data Transfer Objects
 */
package object dtos {
  /**
   * Represents a result of a find operation against the cache.
   * @param path The path of the retrieved result.
   * @param data The data of the retrieved path.
   * @param children The direct children of the retrieved path.
   */
  case class CacheResult(path: String, data: Array[Byte], children: List[String])
}