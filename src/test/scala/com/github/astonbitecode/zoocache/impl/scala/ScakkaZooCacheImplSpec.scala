package com.github.astonbitecode.zoocache.impl.scala

import org.junit.runner.RunWith
import org.specs2.mutable
import org.specs2.runner.JUnitRunner
import org.specs2.mock.Mockito
import akka.actor.ActorSystem
import com.github.astonbitecode.zoocache.api.scala.ScakkaZooCache
import com.github.astonbitecode.zoocache.api.ScakkaException.NotCachedException
import com.github.astonbitecode.zoocache.CacheUpdaterActor.ZkNodeElement
import com.github.astonbitecode.zoocache.Internals.implicits._
import scala.concurrent.Await
import scala.concurrent.duration._
import com.github.astonbitecode.zoocache.zk.impl.ZookeeperInstanceManager
import org.apache.zookeeper.ZooKeeper

@RunWith(classOf[JUnitRunner])
class ScakkaZooCacheImplSpec extends mutable.Specification with Mockito {
  val zk = mock[ZooKeeper]
  val zkManager = new ZookeeperInstanceManager(zk)

  "Tests for the ScakkaZooCache ".txt

  "Get Children " >> {
    "on a non-existing node should " >> {
      "fail" >> {
        val instance = new ScakkaZooCacheImpl(zkManager, ActorSystem("ScakkaZooCache1"))
        instance.getChildren("/") must throwA[NotCachedException]
      }
    }

    " on a an existing node " >> {
      "with no children should return an empty List" >> {
        val instance = new ScakkaZooCacheImpl(zkManager, ActorSystem("ScakkaZooCache2"))
        instance.cache.put("/path", ZkNodeElement("v".getBytes))
        instance.getChildren("/path") must haveSize(0)
      }

      "with children should return a non empty List" >> {
        val instance = ScakkaZooCacheImpl(zkManager, ActorSystem("ScakkaZooCache3"))
        instance.cache.put("/path", new ZkNodeElement("v".getBytes, Set("child1", "child2")))
        instance.getChildren("/path") must haveSize(2)
      }
    }
  }

  "Get Data " >> {
    "on a non-existing node should " >> {
      "fail" >> {
        val instance = new ScakkaZooCacheImpl(zkManager, ActorSystem("ScakkaZooCache4"))
        instance.getData("/") must throwA[NotCachedException]
      }
    }

    " on a an existing node " >> {
      "with no data should return an empty result" >> {
        val instance = new ScakkaZooCacheImpl(zkManager, ActorSystem("ScakkaZooCache5"))
        instance.cache.put("/path", ZkNodeElement("".getBytes))
        instance.getData("/path") must equalTo("".getBytes)
      }

      "with data should return the data of the node" >> {
        val instance = new ScakkaZooCacheImpl(zkManager, ActorSystem("ScakkaZooCache6"))
        instance.cache.put("/path", ZkNodeElement("v".getBytes, Set("child1", "child2")))
        instance.getData("/path") must equalTo("v".getBytes)
      }
    }
  }

  "Add Path to Cache " >> {
    "on a non-existing node should succeed" >> {
      val instance = new ScakkaZooCacheImpl(zkManager, ActorSystem("ScakkaZooCache7"))
      eventually {
        val res = Await.result(instance.addPathToCache("/non/existing/path"), 30.seconds)
        res must beEqualTo(())
      }
    }

    "on an existing node should succeed" >> {
      val instance = new ScakkaZooCacheImpl(zkManager, ActorSystem("ScakkaZooCache8"))
      instance.cache.put("/path", ZkNodeElement("".getBytes))
      eventually {
        val res = Await.result(instance.addPathToCache("/path"), 30.seconds)
        res must beEqualTo(())
      }
    }
  }

  "Remove Path from Cache " >> {
    "on a non-existing node should succeed" >> {
      val instance = new ScakkaZooCacheImpl(zkManager, ActorSystem("ScakkaZooCache9"))
      eventually {
        val res = Await.result(instance.removePathFromCache("/non/existing/path"), 30.seconds)
        res must beEqualTo(())
      }
    }

    "on an existing node should succeed" >> {
      val instance = new ScakkaZooCacheImpl(zkManager, ActorSystem("ScakkaZooCache10"))
      instance.cache.put("/path", ZkNodeElement("".getBytes))
      eventually {
        val res = Await.result(instance.removePathFromCache("/path"), 30.seconds)
        res must beEqualTo(())
      }
    }
  }

  "Shutdown should " >> {
    "succeed" >> {
      val instance = new ScakkaZooCacheImpl(zkManager, ActorSystem("ScakkaZooCache11"))
      instance.cache.put("/path", ZkNodeElement("v".getBytes))
      instance.getChildren("/path") must haveSize(0)

      val unit = Await.result(instance.stop(), 30.seconds)
      unit must beEqualTo(())
      instance.getChildren("/") must throwA[NotCachedException]
    }
  }

  "Find should " >> {
    "return results" >> {
      val instance = new ScakkaZooCacheImpl(zkManager, ActorSystem("ScakkaZooCache12"))
      instance.cache.put("/a/path/1", ZkNodeElement("v1".getBytes, Set("1", "2")))
      instance.cache.put("/a/path/2", ZkNodeElement("v2".getBytes, Set("3", "4")))
      instance.cache.put("/a/otherPath/1", ZkNodeElement("votherPath".getBytes, Set("5", "6")))
      instance.cache.put("/a/path/3", ZkNodeElement("v3".getBytes, Set("7", "8")))

      val results = instance.find("(^\\/a\\/path\\/[\\w]*)")
      results must haveSize(3)

      val resultTuples = results.map { res =>
        {
          (res.path, new String(res.data), res.children)
        }
      }
      resultTuples must contain(("/a/path/1", "v1", List("1", "2")))
      resultTuples must contain(("/a/path/2", "v2", List("3", "4")))
      resultTuples must contain(("/a/path/3", "v3", List("7", "8")))
    }

    "not return results" >> {
      val instance = new ScakkaZooCacheImpl(zkManager, ActorSystem("ScakkaZooCache12"))
      instance.cache.put("/a/path/1", ZkNodeElement("v1".getBytes))
      instance.cache.put("/a/path/2", ZkNodeElement("v2".getBytes))
      instance.cache.put("/a/path/3", ZkNodeElement("v3".getBytes))

      val results = instance.find("(^\\/anohter\\/path\\/[\\w]*)")
      results must beEmpty
    }
  }
}