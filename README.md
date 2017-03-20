# scakka-zoo-cache ![Build Status](https://travis-ci.org/astonbitecode/scakka-zoo-cache.svg?branch=master) [![Maven Central](https://img.shields.io/badge/Maven%20Central-0.2.1-blue.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.astonbitecode%22%20AND%20a%3A%22scakka-zoo-cache%22) [![Maven Central](https://img.shields.io/badge/Maven%20Central-Latest%20Snapshot-blue.svg)](https://oss.sonatype.org/content/repositories/snapshots/com/github/astonbitecode/scakka-zoo-cache/0.2.2-SNAPSHOT/)

A library that caches [ZooKeeper](http://zookeeper.apache.org/) data. The cached data is used __only__ for read operations, namely get Data and get Children. It __does not__ support write operations, these should be done via the ZooKeeper Client. 

The library is intended to be used by applications that heavily use ZooKeeper for read operations and can cope with _eventual consistency_.

It is written in [Scala](http://www.scala-lang.org/), the cache synchronization is internally done using [Akka](http://www.akka.io) and it provides APIs for _Scala_, _Java_ and _Akka_,  all of them easily accessible via a [factory object](https://astonbitecode.github.io/scakka-zoo-cache/#com.github.astonbitecode.zoocache.ScakkaZooCacheFactory$).

The paths to be cached are defined using the library's API. Subtrees and children of the defined paths to cache are _automatically cached_ as well.

Whenever something changes in the ZooKeeper, watches are activated and the cache _eventually follows the change_ (add/remove nodes and data).

## Features

* Built on top of the [ZooKeeper API](http://zookeeper.apache.org/doc/r3.4.9/api/)
* Offering the required abstractions in order to use other ZooKeper frameworks (like [Apache Curator](http://curator.apache.org/))
* Cache the whole ZooKeeper tree, or just parts of it
* Data synchronization using Akka Actors
* Access with [_Scala_](https://astonbitecode.github.io/scakka-zoo-cache/#com.github.astonbitecode.zoocache.api.scala.ScakkaZooCache), [_Akka_](https://astonbitecode.github.io/scakka-zoo-cache/#com.github.astonbitecode.zoocache.api.akka.package), or [_Java_](https://astonbitecode.github.io/scakka-zoo-cache/#com.github.astonbitecode.zoocache.api.java.JScakkaZooCache) APIs.
* Deployed in the Maven Central:

```xml
<dependency>
  <groupId>com.github.astonbitecode</groupId>
  <artifactId>scakka-zoo-cache</artifactId>
  <version>0.2.1</version>
</dependency>
```

Latest snapshot:

```xml
<dependency>
  <groupId>com.github.astonbitecode</groupId>
  <artifactId>scakka-zoo-cache</artifactId>
  <version>0.2.2-SNAPSHOT</version>
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
// Get the children of a path
val children = zooCache.getChildren("/a/path")

// Get the data of a path
val data = zooCache.getData("/a/path")

// Find all the paths that match a regex and return a List of results
val cacheResults = zooCache.find("(^\\/a\\/path\\/[\\w]*)")
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
// Get the children of a path
zooCacheActorRef ! GetChildren("/a/path")

// Get the data of a path
zooCacheActorRef ! GetData("/a/path")

// Find all the paths that match a regex and return a List of results
zooCacheActorRef ! Find("(^\\/a\\/path\\/[\\w]*)")
```
### Akka messaging

The available messages for the Akka API exist in the `com.github.astonbitecode.zoocache.api.akka` package.

Each one of the available Akka messages has its corresponding response. 
This message is sent by the Actor that handles the Akka API as a response to a request. (You can consult the [Scaladocs](https://astonbitecode.github.io/scakka-zoo-cache/) for more details).

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

## Java API Usage

### Initialize the cache

```java
import com.github.astonbitecode.zoocache.ScakkaZooCacheFactory;
import com.github.astonbitecode.zoocache.api.java.JScakkaZooCache;

JScakkaZooCache zooCache = ScakkaZooCacheFactory.java(zk);

```

### Add a Path to the cache

Simply call the addPathToCache:

```java
import java.util.concurrent.Future;

Future<BoxedUnit> f = zooCache.addPathToCache("/a/path");
f.get();

```
### Use the cache

```java
// Get the children of a path
List<String> children = zooCache.getChildren("/a/path");

// Get the data of a path
byte[] data = zooCache.getData("/a/path");

// Find all the paths that match a regex and return a List of results
import com.github.astonbitecode.zoocache.api.dtos.JCacheResult;

List<JCacheResult> cacheResults = zooCache.find("(^\\/a\\/path\\/[\\w]*)");
```
