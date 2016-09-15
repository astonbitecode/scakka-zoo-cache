package com.github.astonbitecode.zoocache

import akka.actor.{ Props, ActorRef, ActorContext, ActorSystem }
import scala.concurrent.ExecutionContextExecutor

private[astonbitecode] object Internals {
  trait ActorCreatable {
    val dispatcher: ExecutionContextExecutor
    def actorOf(props: Props): ActorRef
  }

  case class CreateFromActorContext(actorContext: ActorContext) extends ActorCreatable {
    override val dispatcher: ExecutionContextExecutor = actorContext.dispatcher
    override def actorOf(props: Props): ActorRef = actorContext.actorOf(props)
  }

  case class CreateFromActorSystem(actorSystem: ActorSystem) extends ActorCreatable {
    override val dispatcher: ExecutionContextExecutor = actorSystem.dispatcher
    override def actorOf(props: Props): ActorRef = actorSystem.actorOf(props)
  }

  object implicits {

    implicit def actorContext2ActorCreatable(actorContext: ActorContext): ActorCreatable = {
      CreateFromActorContext(actorContext)
    }

    implicit def actorSystem2ActorCreatable(actorSystem: ActorSystem): ActorCreatable = {
      CreateFromActorSystem(actorSystem)
    }
  }
}