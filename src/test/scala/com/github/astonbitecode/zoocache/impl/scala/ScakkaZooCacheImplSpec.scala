package com.github.astonbitecode.zoocache.impl.scala

import org.junit.runner.RunWith
import org.specs2.mutable
import org.specs2.runner.JUnitRunner
import org.specs2.mock.Mockito
import org.apache.zookeeper.{ ZooKeeper, KeeperException }
import akka.actor.ActorSystem
import com.github.astonbitecode.zoocache.api.scala.ScakkaZooCache
import scala.concurrent.Await
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class ScakkaZooCacheImplSpec extends mutable.Specification with Mockito {
  val zk = mock[ZooKeeper]

  "Tests for the ScakkaZooCache ".txt

  "Get Children " >> {
    "on a non-existing node should " >> {
      "fail" >> {
        val instance = new ScakkaZooCacheImpl(zk, ActorSystem("ScakkaZooCache1"))
        instance.getChildren("/") must throwA[KeeperException]
      }
    }

    " on a an existing node " >> {
      "with no children should return an empty List" >> {
        val instance = new ScakkaZooCacheImpl(zk, ActorSystem("ScakkaZooCache2"))
        instance.cache.put("/path", ScakkaZooCache.ZkNodeElement("v".getBytes))
        instance.getChildren("/path") must haveSize(0)
      }

      "with children should return a non empty List" >> {
        val instance = ScakkaZooCacheImpl(zk, ActorSystem("ScakkaZooCache3"))
        instance.cache.put("/path", new ScakkaZooCache.ZkNodeElement("v".getBytes, Set("child1", "child2")))
        instance.getChildren("/path") must haveSize(2)
      }
    }
  }

  "Get Data " >> {
    "on a non-existing node should " >> {
      "fail" >> {
        val instance = new ScakkaZooCacheImpl(zk, ActorSystem("ScakkaZooCache4"))
        instance.getData("/") must throwA[KeeperException]
      }
    }

    " on a an existing node " >> {
      "with no data should return an empty result" >> {
        val instance = new ScakkaZooCacheImpl(zk, ActorSystem("ScakkaZooCache5"))
        instance.cache.put("/path", ScakkaZooCache.ZkNodeElement("".getBytes))
        instance.getData("/path") must equalTo("".getBytes)
      }

      "with data should return the data of the node" >> {
        val instance = new ScakkaZooCacheImpl(zk, ActorSystem("ScakkaZooCache6"))
        instance.cache.put("/path", ScakkaZooCache.ZkNodeElement("v".getBytes, Set("child1", "child2")))
        instance.getData("/path") must equalTo("v".getBytes)
      }
    }
  }

  "Add Path to Cache " >> {
    "on a non-existing node should succeed" >> {
      val instance = new ScakkaZooCacheImpl(zk, ActorSystem("ScakkaZooCache7"))
      eventually {
        val res = Await.result(instance.addPathToCache("/non/existing/path"), 30.seconds)
        res must beEqualTo(())
      }
    }

    "on an existing node should succeed" >> {
      val instance = new ScakkaZooCacheImpl(zk, ActorSystem("ScakkaZooCache8"))
      instance.cache.put("/path", ScakkaZooCache.ZkNodeElement("".getBytes))
      eventually {
        val res = Await.result(instance.addPathToCache("/path"), 30.seconds)
        res must beEqualTo(())
      }
    }
  }

  "Remove Path from Cache " >> {
    "on a non-existing node should succeed" >> {
      val instance = new ScakkaZooCacheImpl(zk, ActorSystem("ScakkaZooCache9"))
      eventually {
        val res = Await.result(instance.removePathFromCache("/non/existing/path"), 30.seconds)
        res must beEqualTo(())
      }
    }

    "on an existing node should succeed" >> {
      val instance = new ScakkaZooCacheImpl(zk, ActorSystem("ScakkaZooCache10"))
      instance.cache.put("/path", ScakkaZooCache.ZkNodeElement("".getBytes))
      eventually {
        val res = Await.result(instance.removePathFromCache("/path"), 30.seconds)
        res must beEqualTo(())
      }
    }
  }

  "Shutdown should " >> {
    "succeed" >> {
      val instance = new ScakkaZooCacheImpl(zk, ActorSystem("ScakkaZooCache11"))
      instance.cache.put("/path", ScakkaZooCache.ZkNodeElement("v".getBytes))
      instance.getChildren("/path") must haveSize(0)

      val unit = Await.result(instance.stop(), 30.seconds)
      unit must beEqualTo(())
      instance.getChildren("/") must throwA[KeeperException]
    }
  }
}