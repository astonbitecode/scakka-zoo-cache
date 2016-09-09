package com.github.astonbitecode

import scala.concurrent.Promise

package messages {

  private[astonbitecode] case class ScakkaApiWatchUnderPath(path: String, promiseOpt: Option[Promise[Unit]] = None) extends MessageNotifyiable[Unit] {
    override def success(u: Unit): Unit = {
      if (promiseOpt.nonEmpty) {
        promiseOpt.get.success(u)
      }
    }

    override def failure(error: Throwable): Unit = {
      if (promiseOpt.nonEmpty) {
        promiseOpt.get.failure(error)
      }
    }
  }

  private[astonbitecode] case class Add(path: String, value: Array[Byte], updateChildren: Boolean)

  private[astonbitecode] case class Update(path: String, recursive: Boolean)

  private[astonbitecode] case class Remove(path: String)

  private[astonbitecode] case class Unwatch(path: String)
}