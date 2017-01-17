package com.github.astonbitecode.zoocache.api

/**
 * The package contains the message objects that form the Akka API
 */
package object akka {
	import com.github.astonbitecode.zoocache.api.dtos.CacheResult

  /**
   * Gets the children of the node of the given path
   * @param path The path of a node to get the children for
   * @param correlation Optional parameter to help the sender correlating the request with the response
   */
  case class GetChildren(path: String, correlation: Option[Any] = None)

  /**
   * The response message for the GetChildren
   * @param path The path that was specified in the request
   * @param children A List of children of the specified path
   * @param correlation Optional parameter to help the sender correlating the request with the response.
   * This parameter will be the same with the one used in the GetChildren message
   */
  case class GetChildrenResponse(path: String, children: List[String], correlation: Option[Any])

  /**
   * Gets the data of the node of the given path
   * @param path The path of a node to get the data for
   * @param correlation Optional parameter to help the sender correlating the request with the response
   */
  case class GetData(path: String, correlation: Option[Any] = None)

  /**
   * The response message for the GetData
   * @param path The path that was specified in the request
   * @param data An Array of bytes with the data of the specified path
   * @param correlation Optional parameter to help the sender correlating the request with the response.
   * This parameter will be the same with the one used in the GetData message
   */
  case class GetDataResponse(path: String, data: Array[Byte], correlation: Option[Any])

  /**
   * Retrieves the data and children of the paths that match the specified regex.
   * @param: regex The Regular expression to be used in order to find paths in the cache.
   * @param correlation Optional parameter to help the sender correlating the request with the response
   */
  case class Find(regex: String, correlation: Option[Any] = None)

  /**
   * The response message for the Find
   * @param cacheResults The results that are found.
   * @param correlation Optional parameter to help the sender correlating the request with the response.
   * This parameter will be the same with the one used in the GetData message
   */
  case class FindResponse(cacheResults: List[CacheResult], correlation: Option[Any])

  /**
   * Adds a path to the cache. The cache will be updating all the subtree under the defined path.
   * @param path The path to add in the cache
   * @param correlation Optional parameter to help the sender correlating the request with the response
   */
  case class AddPathToCache(path: String, correlation: Option[Any] = None)

  /**
   * The response message for the AddPathToCache
   * @param path The path that was specified in the request
   * @param correlation Optional parameter to help the sender correlating the request with the response.
   * This parameter will be the same with the one used in the AddPathToCache message
   */
  case class AddPathToCacheResponse(path: String, correlation: Option[Any])

  /**
   * Removes a path from the cache. All the paths of the subtree under the defined path will also be removed from the cache and will stop being updated.
   * @param path The to remove from the cache
   * @param correlation Optional parameter to help the sender correlating the request with the response
   */
  case class RemovePathFromCache(path: String, correlation: Option[Any] = None)

  /**
   * The response message for the RemovePathFromCache
   * @param path The path that was specified in the request
   * @param correlation Optional parameter to help the sender correlating the request with the response.
   * This parameter will be the same with the one used in the RemovePathFromCache message
   */
  case class RemovePathFromCacheResponse(path: String, correlation: Option[Any])

  /**
   * Stops and shuts down the cache resources
   * @param correlation Optional parameter to help the sender correlating the request with the response
   */
  case class Stop(correlation: Option[Any] = None)

  /**
   * The response message for the Stop
   * @param correlation Optional parameter to help the sender correlating the request with the response.
   * This parameter will be the same with the one used in the Stop message
   */
  case class StopResponse(correlation: Option[Any])

  /**
   * The response in cases of failures.
   * @param correlation Optional parameter to help the sender correlating the request with the response.
   */
  case class CacheFailure(cause: Throwable, correlation: Option[Any])
}