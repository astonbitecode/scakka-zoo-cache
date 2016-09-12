package com.github.astonbitecode

import scala.concurrent.Promise

package messages {

  private[astonbitecode] case class ScakkaApiWatchUnderPath(path: String, promiseOpt: Option[Promise[Unit]]) extends MessageNotifyiable {
    override def success(): Unit = {
      if (promiseOpt.nonEmpty) {
        promiseOpt.get.success()
      }
    }

    override def failure(error: Throwable): Unit = {
      if (promiseOpt.nonEmpty) {
        promiseOpt.get.failure(error)
      }
    }

    override def getPath(): String = {
      path
    }
  }

  private[astonbitecode] case class Add(path: String, value: Array[Byte], updateChildren: Boolean, notifyOpt: Option[MessageNotifyiable])

  private[astonbitecode] case class Update(path: String, recursive: Boolean, notifyOpt: Option[MessageNotifyiable])

  private[astonbitecode] case class SetWatcher(path: String, notifyOpt: Option[MessageNotifyiable])

  private[astonbitecode] case class Remove(path: String, notifyOpt: Option[MessageNotifyiable])

  private[astonbitecode] case class Unwatch(path: String, notifyOpt: Option[MessageNotifyiable])
}