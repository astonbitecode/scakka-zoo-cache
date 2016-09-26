package com.github.astonbitecode.zoocache

/**
 * Does the needed actions (if needed) to notify the caller
 */
private[astonbitecode] trait MessageNotifyable {
  /**
   * Do stuff once the message handling was successful
   * @param path The path for which the operation was successful
   * @return Boolean True if the success logic was indeed invoked, false otherwise. This is needed because the
   * method needs to be idempotent. In reality the message should notify the user for success or failure only once.
   * However, the method may be called many times.
   */
  def success(path: String): Boolean
  /**
   * Do stuff once the message handling encountered an error
   * @param path The path for which the operation had failed
   * @return Boolean True if the failure logic was indeed invoked, false otherwise. This is needed because the
   * method needs to be idempotent. In reality the message should notify the user for success or failure only once.
   * However, the method may be called many times.
   */
  def failure(path: String, error: Throwable): Boolean
}