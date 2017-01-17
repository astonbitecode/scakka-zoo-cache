package com.github.astonbitecode.zoocache.impl.akka

import org.junit.runner.RunWith
import org.specs2.mutable
import org.specs2.runner.JUnitRunner
import org.specs2.mock.Mockito
import com.github.astonbitecode.zoocache.api.scala.ScakkaZooCache
import com.github.astonbitecode.zoocache.api.akka._
import com.github.astonbitecode.zoocache.api.ScakkaException.NotCachedException
import akka.actor.ActorSystem
import akka.testkit.TestProbe
import scala.concurrent.duration._
import scala.concurrent.Future
import com.github.astonbitecode.zoocache.api.dtos.CacheResult

@RunWith(classOf[JUnitRunner])
class ScakkaZooCacheActorSpec extends mutable.Specification with Mockito {
  "Tests for the ScakkaZooCache Actor ".txt

  "Get Children " >> {
    "should fail" >> {
      val zooCache = mock[ScakkaZooCache]
      zooCache.getChildren(any).throws(new NotCachedException(""))
      implicit val actorSystem = ActorSystem()
      val actor = actorSystem.actorOf(ScakkaZooCacheActor.props(zooCache))
      val probe = TestProbe()
      probe.send(actor, GetChildren("/a/path", Some("123")))
      val response = probe.receiveOne(10.seconds)
      response must beAnInstanceOf[CacheFailure]
      val failure = response.asInstanceOf[CacheFailure]
      failure.correlation must beEqualTo(Some("123"))
      failure.cause must beAnInstanceOf[NotCachedException]
    }

    "should succeed" >> {
      val zooCache = mock[ScakkaZooCache]
      val children = List("child1", "child2", "child3")
      zooCache.getChildren(any).returns(children)
      implicit val actorSystem = ActorSystem()
      val actor = actorSystem.actorOf(ScakkaZooCacheActor.props(zooCache))
      val probe = TestProbe()
      probe.send(actor, GetChildren("/a/path", Some("456")))
      val response = probe.receiveOne(10.seconds)
      response must beAnInstanceOf[GetChildrenResponse]
      val getChildrenResponse = response.asInstanceOf[GetChildrenResponse]
      getChildrenResponse.correlation must beEqualTo(Some("456"))
      getChildrenResponse.children must beEqualTo(children)
    }
  }

  "Get Data " >> {
    "should fail" >> {
      val zooCache = mock[ScakkaZooCache]
      zooCache.getData(any).throws(new NotCachedException(""))
      implicit val actorSystem = ActorSystem()
      val actor = actorSystem.actorOf(ScakkaZooCacheActor.props(zooCache))
      val probe = TestProbe()
      probe.send(actor, GetData("/a/path", Some("111")))
      val response = probe.receiveOne(10.seconds)
      response must beAnInstanceOf[CacheFailure]
      val failure = response.asInstanceOf[CacheFailure]
      failure.correlation must beEqualTo(Some("111"))
      failure.cause must beAnInstanceOf[NotCachedException]
    }

    "should succeed" >> {
      val zooCache = mock[ScakkaZooCache]
      val data = "This is the data"
      zooCache.getData(any).returns(data.getBytes)
      implicit val actorSystem = ActorSystem()
      val actor = actorSystem.actorOf(ScakkaZooCacheActor.props(zooCache))
      val probe = TestProbe()
      probe.send(actor, GetData("/a/path", Some("222")))
      val response = probe.receiveOne(10.seconds)
      response must beAnInstanceOf[GetDataResponse]
      val getDataResponse = response.asInstanceOf[GetDataResponse]
      getDataResponse.correlation must beEqualTo(Some("222"))
      new String(getDataResponse.data) must beEqualTo(data)
    }
  }

  "Add path to cache " >> {
    "should fail" >> {
      val zooCache = mock[ScakkaZooCache]
      implicit val actorSystem = ActorSystem()
      implicit val ec = actorSystem.dispatcher
      zooCache.addPathToCache(any).returns { Future { throw new RuntimeException("") } }
      val probe = TestProbe()
      val actor = actorSystem.actorOf(ScakkaZooCacheActor.props(zooCache))
      probe.send(actor, AddPathToCache("/a/path", Some("333")))
      val response = probe.receiveOne(10.seconds)
      response must beAnInstanceOf[CacheFailure]
      val failure = response.asInstanceOf[CacheFailure]
      failure.correlation must beEqualTo(Some("333"))
      failure.cause must beAnInstanceOf[RuntimeException]
    }

    "should succeed" >> {
      val zooCache = mock[ScakkaZooCache]
      implicit val actorSystem = ActorSystem()
      implicit val ec = actorSystem.dispatcher
      val data = "This is the data"
      zooCache.addPathToCache(any).returns { Future {} }
      val actor = actorSystem.actorOf(ScakkaZooCacheActor.props(zooCache))
      val probe = TestProbe()
      probe.send(actor, AddPathToCache("/a/path", Some("3232")))
      val response = probe.receiveOne(10.seconds)
      response must beAnInstanceOf[AddPathToCacheResponse]
      val addPathToCacheResponse = response.asInstanceOf[AddPathToCacheResponse]
      addPathToCacheResponse.correlation must beEqualTo(Some("3232"))
    }
  }

  "Remove path from cache " >> {
    "should fail" >> {
      val zooCache = mock[ScakkaZooCache]
      implicit val actorSystem = ActorSystem()
      implicit val ec = actorSystem.dispatcher
      zooCache.removePathFromCache(any).returns { Future { throw new RuntimeException("") } }
      val probe = TestProbe()
      val actor = actorSystem.actorOf(ScakkaZooCacheActor.props(zooCache))
      probe.send(actor, RemovePathFromCache("/a/path", Some("3636")))
      val response = probe.receiveOne(10.seconds)
      response must beAnInstanceOf[CacheFailure]
      val failure = response.asInstanceOf[CacheFailure]
      failure.correlation must beEqualTo(Some("3636"))
      failure.cause must beAnInstanceOf[RuntimeException]
    }

    "should succeed" >> {
      val zooCache = mock[ScakkaZooCache]
      implicit val actorSystem = ActorSystem()
      implicit val ec = actorSystem.dispatcher
      val data = "This is the data"
      zooCache.removePathFromCache(any).returns { Future {} }
      val actor = actorSystem.actorOf(ScakkaZooCacheActor.props(zooCache))
      val probe = TestProbe()
      probe.send(actor, RemovePathFromCache("/a/path", Some("3737")))
      val response = probe.receiveOne(10.seconds)
      response must beAnInstanceOf[RemovePathFromCacheResponse]
      val removePathFromacheResponse = response.asInstanceOf[RemovePathFromCacheResponse]
      removePathFromacheResponse.correlation must beEqualTo(Some("3737"))
    }
  }

  "Stop " >> {
    "should fail" >> {
      val zooCache = mock[ScakkaZooCache]
      implicit val actorSystem = ActorSystem()
      implicit val ec = actorSystem.dispatcher
      zooCache.stop().returns { Future { throw new RuntimeException("") } }
      val probe = TestProbe()
      val actor = actorSystem.actorOf(ScakkaZooCacheActor.props(zooCache))
      probe.send(actor, Stop(Some("1111")))
      val response = probe.receiveOne(10.seconds)
      response must beAnInstanceOf[CacheFailure]
      val failure = response.asInstanceOf[CacheFailure]
      failure.correlation must beEqualTo(Some("1111"))
      failure.cause must beAnInstanceOf[RuntimeException]
    }

    "should succeed" >> {
      val zooCache = mock[ScakkaZooCache]
      implicit val actorSystem = ActorSystem()
      implicit val ec = actorSystem.dispatcher
      val data = "This is the data"
      zooCache.stop().returns { Future {} }
      val actor = actorSystem.actorOf(ScakkaZooCacheActor.props(zooCache))
      val probe = TestProbe()
      probe.send(actor, Stop(Some("1112")))
      val response = probe.receiveOne(10.seconds)
      response must beAnInstanceOf[StopResponse]
      val stopResponse = response.asInstanceOf[StopResponse]
      stopResponse.correlation must beEqualTo(Some("1112"))
    }
  }

  "Find should " >> {
    "return results" >> {
      val zooCache = mock[ScakkaZooCache]
      val data = "This is the data"
      val cr = CacheResult("/a/path", "data".getBytes, List("1", "2"))
      zooCache.find(any).returns(List(cr))
      implicit val actorSystem = ActorSystem()
      val actor = actorSystem.actorOf(ScakkaZooCacheActor.props(zooCache))
      val probe = TestProbe()
      probe.send(actor, Find("/a/path", Some("333333")))
      val response = probe.receiveOne(10.seconds)
      response must beAnInstanceOf[FindResponse]
      val findResponse = response.asInstanceOf[FindResponse]
      findResponse.correlation must beEqualTo(Some("333333"))
      findResponse.cacheResults must haveSize(1)
    }
  }
}