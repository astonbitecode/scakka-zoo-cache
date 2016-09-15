# scakka-zoo-cache ![Build Status](https://travis-ci.org/astonbitecode/scakka-zoo-cache.svg?branch=master)

A library that caches [ZooKeeper](http://zookeeper.apache.org/) data.

It is written in [Scala](http://www.scala-lang.org/) and the cache synchronization is done using [Akka](http://www.akka.io).

The library caches data using a [ZooKeeper](http://zookeeper.apache.org/doc/r3.4.9/api/index.html) instance, by specifying watches in the `znode`s that should be cached.

The paths and the subtrees to cache are defined using the library's API.

## Features

* Built on top of the [ZooKeeper API](http://zookeeper.apache.org/doc/r3.4.9/api/)
* Cache the whole ZooKeeper tree, or just parts of it
* Data synchronization using Akka Actors
* Access with Scala, Akka, or Java messaging API

## Scala API Usage

### Initialize the cache

Assuming that _zk_ is a `ZooKeeper` class instance, a `ScakkaZooCache` can be created like following:

####1.  Using simple initialization

```
import com.github.astonbitecode.zoocache.api.scala.ScakkaZooCache

val zooCache = ScakkaZooCache(zk)
```
####2. Defining an ActorSystem

```
import com.github.astonbitecode.zoocache.api.scala.ScakkaZooCache

val actorSystem = ActorSystem("myActorSystem")
val zooCache = ScakkaZooCache(zk, actorSystem)
```

### Add a Path to the cache

Simply call the _addPathToCache_:

`zooCache.addPathToCache("/cache/this/path")`

### Use the cache

```
val children = zooCache.getChildren("/a/path")
val data = zooCache.getData("/a/path")
```

