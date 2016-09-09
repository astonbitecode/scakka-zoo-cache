package com.github.astonbitecode

/**
 * Does the needed actions (if needed) to notify the caller
 */
private[astonbitecode] trait MessageNotifyiable[T] {
  /**
   * Do things once the message handling was successful
   */
  def success(t: T): Unit
  /**
   * Do things once the message handling encountered an error
   */
  def failure(error: Throwable): Unit
}