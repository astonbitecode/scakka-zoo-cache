package com.github.astonbitecode

import org.junit.runner.RunWith
import org.specs2.mutable
import org.specs2.runner.JUnitRunner
import org.specs2.mock.Mockito
import org.apache.zookeeper.{ ZooKeeper, KeeperException }
import akka.actor.ActorSystem
import com.github.astonbitecode.impl.scala.ScakkaZooCacheImpl
import com.github.astonbitecode.api.scala.ScakkaZooCache

@RunWith(classOf[JUnitRunner])
class ScakkaZooCacheImplSpec extends mutable.Specification with Mockito {
  val zk = mock[ZooKeeper]

  "Tests for the ScakkaZooCache ".txt

  "Get Children " >> {
    "on a non-existing node should " >> {
      "fail" >> {
        val instance = new ScakkaZooCacheImpl(zk, ActorSystem("ScakkaZooCache"))
        instance.getChildren("/") must throwA[KeeperException]
      }
    }

    " on a an existing node " >> {
      "with no children should return an empty List" >> {
        val instance = new ScakkaZooCacheImpl(zk, ActorSystem("ScakkaZooCache"))
        instance.cache.put("/path", ScakkaZooCache.ZkNodeElement("v".getBytes))
        instance.getChildren("/path") must haveSize(0)
      }

      "with children should return a non empty List" >> {
        val instance = ScakkaZooCacheImpl(zk, ActorSystem("ScakkaZooCache"))
        instance.cache.put("/path", new ScakkaZooCache.ZkNodeElement("v".getBytes, Set("child1", "child2")))
        instance.getChildren("/path") must haveSize(2)
      }
    }
  }

  "Get Data " >> {
    "on a non-existing node should " >> {
      "fail" >> {
        val instance = new ScakkaZooCacheImpl(zk, ActorSystem("ScakkaZooCache"))
        instance.getData("/") must throwA[KeeperException]
      }
    }

    " on a an existing node " >> {
      "with no data should return an empty result" >> {
        val instance = new ScakkaZooCacheImpl(zk, ActorSystem("ScakkaZooCache"))
        instance.cache.put("/path", ScakkaZooCache.ZkNodeElement("".getBytes))
        instance.getData("/path") must equalTo("".getBytes)
      }

      "with data should return the data of the node" >> {
        val instance = new ScakkaZooCacheImpl(zk, ActorSystem("ScakkaZooCache"))
        instance.cache.put("/path", ScakkaZooCache.ZkNodeElement("v".getBytes, Set("child1", "child2")))
        instance.getData("/path") must equalTo("v".getBytes)
      }
    }
  }

}