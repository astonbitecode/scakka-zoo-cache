package com.github.astonbitecode.zoocache

import org.junit.runner.RunWith
import org.specs2.mutable
import org.specs2.runner.JUnitRunner
import org.specs2.mock.Mockito
import org.apache.zookeeper.ZooKeeper
import akka.actor.{ ActorSystem, Actor, Props }
import akka.pattern.ask
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.util.Timeout

@RunWith(classOf[JUnitRunner])
class ScakkaZooCacheFactorySpec extends mutable.Specification with Mockito {
  val zk = mock[ZooKeeper]

  "Tests for the ScakkaZooCacheFactory ".txt

  "Creation of " >> {

    "the Scala API " >> {
      " without ActorSystem" >> {
        ScakkaZooCacheFactory.scala(zk) must not be null
      }

      " with ActorSystem" >> {
        val actorSystem = ActorSystem()
        ScakkaZooCacheFactory.scala(zk, actorSystem) must not be null
      }

      " with ActorContext" >> {
        class CreatorActor extends Actor {
          override def receive(): Receive = {
            case _ => sender ! ScakkaZooCacheFactory.scala(zk, context)
          }
        }

        val actorSystem = ActorSystem()
        val creatorActor = actorSystem.actorOf(Props(new CreatorActor()))
        implicit val timeout = Timeout(10.seconds)
        val created = Await.result((creatorActor ? ""), timeout.duration)
        created must not be null
      }
    }

    "the Akka API " >> {
      ScakkaZooCacheFactory.props(ScakkaZooCacheFactory.scala(zk)) must not be null
    }

    "the Java API " >> {
      ScakkaZooCacheFactory.java(zk) must not be null
    }
  }
}