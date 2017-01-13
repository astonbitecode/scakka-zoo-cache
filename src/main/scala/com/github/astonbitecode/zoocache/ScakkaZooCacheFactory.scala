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
import com.github.astonbitecode.zoocache.zk.impl.ZookeeperInstanceManager
import com.github.astonbitecode.zoocache.zk.ZookeeperManager

object ScakkaZooCacheFactory {

  /**
   * Creates a ScakkaZooCache that offers the Scala API.
   * The ActorSystem that is needed for creating the internal Actors, is being automatically created.
   * @param zoo A ZooKeeper instance
   * @return A ScakkaZooCache instance
   */
  def scala(zoo: ZooKeeper): ScakkaZooCache = {
    val actorSystem = ActorSystem("ScakkaZooCache_" + UUID.randomUUID().toString())
    new ScakkaZooCacheImpl(new ZookeeperInstanceManager(zoo), actorSystem)
  }

  /**
   * Creates a ScakkaZooCache that offers the Scala API.
   * The ActorSystem that is needed for creating the internal Actors, is being automatically created.
   * @param zookeeperManager A ZooKeeperManager implementation
   * @return A ScakkaZooCache instance
   */
  def scala(zookeeperManager: ZookeeperManager): ScakkaZooCache = {
    val actorSystem = ActorSystem("ScakkaZooCache_" + UUID.randomUUID().toString())
    new ScakkaZooCacheImpl(zookeeperManager, actorSystem)
  }

  /**
   * Creates a ScakkaZooCache that offers the Scala API.
   * @param zoo A ZooKeeper instance
   * @param actorSystem The ActorSystem to use for creating the internal Actors
   * @return A ScakkaZooCache instance
   */
  def scala(zoo: ZooKeeper, actorSystem: ActorSystem): ScakkaZooCache = {
    new ScakkaZooCacheImpl(new ZookeeperInstanceManager(zoo), actorSystem)
  }

  /**
   * Creates a ScakkaZooCache that offers the Scala API.
   * @param zookeeperManager A ZooKeeperManager implementation
   * @param actorSystem The ActorSystem to use for creating the internal Actors
   * @return A ScakkaZooCache instance
   */
  def scala(zookeeperManager: ZookeeperManager, actorSystem: ActorSystem): ScakkaZooCache = {
    new ScakkaZooCacheImpl(zookeeperManager, actorSystem)
  }

  /**
   * Creates a ScakkaZooCache that offers the Scala API.
   * @param zoo A ZooKeeper instance
   * @param actorContext The ActorContext to use for creating the internal Actors
   * @return A ScakkaZooCache instance
   */
  def scala(zoo: ZooKeeper, actorContext: ActorContext): ScakkaZooCache = {
    new ScakkaZooCacheImpl(new ZookeeperInstanceManager(zoo), actorContext)
  }

  /**
   * Creates a ScakkaZooCache that offers the Scala API.
   * @param zookeeperManager A ZooKeeperManager implementation
   * @param actorContext The ActorContext to use for creating the internal Actors
   * @return A ScakkaZooCache instance
   */
  def scala(zookeeperManager: ZookeeperManager, actorContext: ActorContext): ScakkaZooCache = {
    new ScakkaZooCacheImpl(zookeeperManager, actorContext)
  }

  /**
   * Returns a Props in order to be used for the Actor responsible for Handling the Akka API messages
   * @return A ScakkaZooCache instance
   */
  def props(scakkaZooCache: ScakkaZooCache): Props = {
    ScakkaZooCacheActor.props(scakkaZooCache)
  }

  /**
   * Creates a ScakkaZooCache that offers the Java API.
   * The ActorSystem that is needed for creating the internal Actors, is being automatically created.
   * @param zoo A ZooKeeper instance
   * @return A ScakkaZooCache instance
   */
  def java(zoo: ZooKeeper): JScakkaZooCache = {
    val scakkaCache = ScakkaZooCacheFactory.scala(zoo)
    new ScakkaZooCacheJavaImpl(scakkaCache)
  }

  /**
   * Creates a ScakkaZooCache that offers the Java API.
   * The ActorSystem that is needed for creating the internal Actors, is being automatically created.
   * @param zookeeperManager A ZooKeeperManager implementation
   * @return A ScakkaZooCache instance
   */
  def java(zookeeperManager: ZookeeperManager): JScakkaZooCache = {
    val scakkaCache = ScakkaZooCacheFactory.scala(zookeeperManager)
    new ScakkaZooCacheJavaImpl(scakkaCache)
  }
}