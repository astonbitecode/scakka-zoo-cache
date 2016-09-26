# scakka-zoo-cache ![Build Status](https://travis-ci.org/astonbitecode/scakka-zoo-cache.svg?branch=master)

A library that caches [ZooKeeper](http://zookeeper.apache.org/) data.

It is written in [Scala](http://www.scala-lang.org/) and the cache synchronization is done using [Akka](http://www.akka.io).

The library caches data using a [ZooKeeper](http://zookeeper.apache.org/doc/r3.4.9/api/index.html) instance, by specifying watches in the `znode`s that should be cached.

The paths to be cached are defined using the library's API. Subtrees and children of the defined paths to cache are _automatically cached_ as well.

Whenever something changes in the ZooKeeper, watches are activated and the cache _eventually follows the change_ (add/remove nodes and data).

## Features

* Built on top of the [ZooKeeper API](http://zookeeper.apache.org/doc/r3.4.9/api/)
* Cache the whole ZooKeeper tree, or just parts of it
* Data synchronization using Akka Actors
* Access with Scala, Akka, or Java APIs
* Deployed in the Maven Central:

```xml
<dependency>
  <groupId>com.github.astonbitecode</groupId>
  <artifactId>scakka-zoo-cache</artifactId>
  <version>0.1.0</version>
</dependency>
```

## API Usage

The APIs of the ScakkaZooCache are offered for _Scala_, _Akka_ and _Java_, depending on how the cache is created (which `com.github.astonbitecode.zoocache.ScakkaZooCacheFactory` method will be used for the instantiation of the cache). 

They all support the following:

* Adding a path to the cache

	After adding a path to the cache (eg. `/path/one`, all the ZooKeeper changes under the path `/path/one` will be monitored.
	So, for example when getting the data of the path `/path/one/child1` the Data of the zNode will be returned from the cache.
	Of course, this will happen __if and only if__ the path really exists in the ZooKeeper.
	In the opposite case, the call will throw a `NotCachedException`.
* Reading children of a path.

* Getting data of a node in a path.

* Removing a path from the cache

	Stop monitoring a path and remove it from the cache.
* Stopping the cache

___Note:___ _Writing to the ZooKeeper is not done via the ScakkaZooCache. It should be done by using the ZooKeeper client itself. The cache is used_ ___only for reading operations___.

---

## Scala API Usage

### Initialize the cache

Assuming that _zk_ is a `ZooKeeper` class instance, a `ScakkaZooCache` can be created like following:

####1.  Using simple initialization

```scala
import com.github.astonbitecode.zoocache.ScakkaZooCacheFactory

val zooCache = ScakkaZooCacheFactory.scala(zk)
```
####2. Defining an ActorSystem

```scala
import com.github.astonbitecode.zoocache.ScakkaZooCacheFactory

val actorSystem = ActorSystem("myActorSystem")
val zooCache = ScakkaZooCacheFactory.scala(zk, actorSystem)
```

####3. Creating from inside an Akka Actor, using the ActorContext

```scala
import com.github.astonbitecode.zoocache.ScakkaZooCacheFactory

// This is one Actor
class MyActor extends Actor {

  // Create a zoocache instance
  val zooCache = ScakkaZooCacheFactory.scala(zk, context)

  // Handle messages
  override def receive(): Receive = {
    case _ => ... // Use the zoo cache
  }
}

```

### Add a Path to the cache

Simply call the _addPathToCache_:

`zooCache.addPathToCache("/path/one")`

### Use the cache

```scala
val children = zooCache.getChildren("/a/path")
val data = zooCache.getData("/a/path")
```

## Akka API Usage

### Create the Actor that handles the Akka API messages

```scala
import com.github.astonbitecode.zoocache.ScakkaZooCacheFactory

// Create your ActorSystem
val actorSystem = ActorSystem("myActorSystem")

// Create a ScakkaZooCache
val zooCache = ScakkaZooCacheFactory.scala(zk, actorSystem)

// Get the Akka Props from the factory
val props = ScakkaZooCacheFactory.props()

// Create the ActorRef
val zooCacheActorRef = actorSystem.actorOf(props)

// Contact the ActorRef
zooCacheActorRef ! GetChildren("/a/path")
```

### Add a path to the cache
```scala
zooCacheActorRef ! AddPathToCache("/a/path")
```

### Use the cache

```scala
zooCacheActorRef ! GetChildren("/a/path")
zooCacheActorRef ! GetData("/a/path")
```
### Akka messaging

The available messages for the Akka API exist in the `com.github.astonbitecode.zoocache.api.akka` package.

Each one of the available Akka messages has its corresponding response. 
This message is sent by the Actor that handles the Akka API as a response to a request. (You can consult the Scaladocs for more details).

For example, when sending a `GetChildren` message, a response of `GetChildrenResponse` will be received:

```scala
val askFuture = zooCacheActorRef ? GetChildren("/a/path")
val children: GetChildrenResponse = Await.result(askFuture, 30 seconds)
```

### Request - Response correlation

Akka users are not obliged to use the Ask pattern. All the Akka messages offered by the ScakkaZooCache API have an Optional parameter that can be used for Request-Response correlation:

```scala
import com.github.astonbitecode.zoocache.ScakkaZooCacheFactory
import com.github.astonbitecode.zoocache.api.akka._

// This is one Actor
class MyActor extends Actor {

  // Create a zoocache instance
  val zooCache = ScakkaZooCacheFactory.scala(zk, context)
  // Create the zoocache Actor to handle the Akka API messages
  val zooCacheActorRef = context.actorOf(ScakkaZooCacheFactory.props(zooCache))

  // Send a message to the zooCacheActor
  zooCacheActorRef ! GetChildren("/a/path", Some("123"))

  // Handle the Responses
  override def receive(): Receive = {
    case getChildrenResponse: GetChildrenResponse => {
      if (getChildrenResponse.correlation == Some("123")) {
        println("OK")
      } else {
        println("CORRELATION ERROR")
      }
    }
    case _ =>
  }
}
```

### Failure cases

In case of failure of any of the Akka API messages, the sender will receive a `CacheFailure`.

This message contains the failure details along with any correlation object the respective request contained.



