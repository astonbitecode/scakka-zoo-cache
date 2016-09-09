package com.github.astonbitecode

import org.junit.runner.RunWith
import org.specs2.mutable
import org.specs2.runner.JUnitRunner
import org.specs2.mock.Mockito
import org.apache.zookeeper.{ ZooKeeper, KeeperException }

@RunWith(classOf[JUnitRunner])
class ScakkaZooCacheSpec extends mutable.Specification with Mockito {
  val zk = mock[ZooKeeper]

  "Tests for the ScakkaZooCache ".txt

  "Get Children on a non-existing node should fail" >> {
    val instance = ScakkaZooCache(zk)
    instance.getChildren("/") must throwA[KeeperException]
  }

  "Get Children on an existing node with no children should return an empty List" >> {
    val instance = ScakkaZooCache(zk)
    instance.cache.put("/path", ScakkaZooCache.ZkNodeElement("v".getBytes))
    instance.getChildren("/path") must haveSize(0)
  }

  "Get Children on an existing node with children should return a non empty List" >> {
    val instance = ScakkaZooCache(zk)
    instance.cache.put("/path", ScakkaZooCache.ZkNodeElement("v".getBytes, Set("child1", "child2")))
    instance.getChildren("/path") must haveSize(2)
  }
}