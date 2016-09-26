package com.github.astonbitecode.zoocache

import com.github.astonbitecode.zoocache.api.scala.ScakkaZooCache
import com.github.astonbitecode.zoocache.impl.scala.ScakkaZooCacheImpl
import com.github.astonbitecode.zoocache.impl.akka.ScakkaZooCacheActor
import com.github.astonbitecode.zoocache.Internals.implicits._
import akka.actor.{
  ActorSystem,
  ActorContext,
  Props
}
import org.apache.zookeeper.ZooKeeper
import java.util.UUID
import com.github.astonbitecode.zoocache.api.java.JScakkaZooCache
import com.github.astonbitecode.zoocache.impl.java.ScakkaZooCacheJavaImpl

object ScakkaZooCacheFactory {

  /**
   * Creates a ScakkaZooCache that offers the Scala API.
   * The ActorSystem that is needed for creating the internal Actors, is being automatically created.
   * @param zoo A ZooKeeper instance
   */
  def scala(zoo: ZooKeeper): ScakkaZooCache = {
    val actorSystem = ActorSystem("ScakkaZooCache_" + UUID.randomUUID().toString())
    new ScakkaZooCacheImpl(zoo, actorSystem)
  }

  /**
   * Creates a ScakkaZooCache that offers the Scala API.
   * @param zoo A ZooKeeper instance
   * @param actorSystem The ActorSystem to use for creating the internal Actors
   */
  def scala(zoo: ZooKeeper, actorSystem: ActorSystem): ScakkaZooCache = {
    new ScakkaZooCacheImpl(zoo, actorSystem)
  }

  /**
   * Creates a ScakkaZooCache that offers the Scala API.
   * @param zoo A ZooKeeper instance
   * @param actorContext The ActorContext to use for creating the internal Actors
   */
  def scala(zoo: ZooKeeper, actorContext: ActorContext): ScakkaZooCache = {
    new ScakkaZooCacheImpl(zoo, actorContext)
  }

  /**
   * Returns a Props in order to be used for the Actor responsible for Handling the Akka API messages
   */
  def props(scakkaZooCache: ScakkaZooCache): Props = {
    ScakkaZooCacheActor.props(scakkaZooCache)
  }

  /**
   * Creates a ScakkaZooCache that offers the Java API.
   * The ActorSystem that is needed for creating the internal Actors, is being automatically created.
   * @param zoo A ZooKeeper instance
   */
  def java(zoo: ZooKeeper): JScakkaZooCache = {
    val scakkaCache = ScakkaZooCacheFactory.scala(zoo)
    new ScakkaZooCacheJavaImpl(scakkaCache)
  }
}