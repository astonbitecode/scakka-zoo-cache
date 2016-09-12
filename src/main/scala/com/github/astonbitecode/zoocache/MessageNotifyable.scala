package com.github.astonbitecode.zoocache

/**
 * Does the needed actions (if needed) to notify the caller
 */
private[astonbitecode] trait MessageNotifyable {
  /**
   * Do things once the message handling was successful
   */
  def success(path: String): Unit
  /**
   * Do things once the message handling encountered an error
   */
  def failure(path: String, error: Throwable): Unit
}