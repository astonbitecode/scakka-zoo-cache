package com.github.astonbitecode.zoocache.zk

import org.apache.zookeeper.Watcher

trait ZookeeperManager {
  /**
   * Registers a watcher.
   */
  def registerWatcher(watcher: Watcher)

  /**
   * Set the watchers for the specified path.
   * Watchers should bcase e set for the following EventTypes:
   * Event.EventType.NodeCreated
   * Event.EventType.NodeDeleted
   * Event.EventType.NodeDataChanged
   * Event.EventType.NodeChildrenChanged
   * @param path The path to set the watchers for.
   */
  def setWatchers(path: String): Unit

  /**
   * Returns a List of Strings that represent the children of a node.
   * @param path The path to get the children for.
   */
  def getChildren(path: String): Set[String]

  /**
   * Returns the data of the specified path.
   * @param path The path to get the data for.
   */
  def getData(path: String): Array[Byte]
  
}