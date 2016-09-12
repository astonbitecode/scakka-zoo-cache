package com.github.astonbitecode

/**
 * Does the needed actions (if needed) to notify the caller
 */
private[astonbitecode] trait MessageNotifyiable {
  /**
   * Do things once the message handling was successful
   */
  def success(): Unit
  /**
   * Do things once the message handling encountered an error
   */
  def failure(error: Throwable): Unit
  /**
   * Returns the path for which the Notifyable applies
   */
  def getPath(): String
}