package tests.integration

import org.junit.runner.RunWith
import org.specs2.mutable
import org.specs2.runner.JUnitRunner
import org.apache.zookeeper.{
  ZooKeeper,
  ZooDefs,
  CreateMode
}
import org.apache.curator.test.TestingServer
import com.github.astonbitecode.zoocache.api.ScakkaException.NotCachedException
import com.github.astonbitecode.zoocache.ScakkaZooCacheFactory
import scala.concurrent.Await
import scala.concurrent.duration._
import java.util.ArrayList
import org.apache.zookeeper.data.ACL
import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.specs2.specification.AfterAll

@RunWith(classOf[JUnitRunner])
class SanitySpec extends mutable.Specification with AfterAll {
  val server = new TestingServer(true)
  val zk: ZooKeeper = new ZooKeeper(server.getConnectString, 1000, null)
  val instance = ScakkaZooCacheFactory.scala(zk)

  override def afterAll() {
    Await.result(instance.stop(), 30.seconds)
    zk.close()
    server.close()
  }

  "Integration test - Sanity ".txt

  "Initialization should " >> {
    "succeed" >> {
      zk.create("/path1", "".getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL)
      Await.result(instance.addPathToCache("/path1"), 30.seconds)
      eventually {
        instance.getChildren("/path1") must haveSize(0)
      }
    }
  }

  "Cache should " >> {
    "synchronize values from the ZooKeeper after a Path is defined and exists" >> {
      zk.create("/path2", "".getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
      Await.result(instance.addPathToCache("/path2"), 30.seconds)
      eventually {
        instance.getChildren("/path2") must haveSize(0)
      }

      zk.create("/path2/child1", "".getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL)
      eventually {
        instance.getChildren("/path2") must haveSize(1)
      }
    }

    "allow adding a path even if this path does not actually exist in the ZooKeeper" >> {
      Await.result(instance.addPathToCache("/path3"), 30.seconds)
      // Need to wait in order to avoid false positive
      Thread.sleep(1000)
      zk.create("/path3", "".getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
      eventually {
        instance.getChildren("/path3") must haveSize(0)
      }

      zk.create("/path3/child1", "".getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL)
      eventually {
        instance.getChildren("/path3") must haveSize(1)
      }
    }

    "synchronize the data of a znode from the ZooKeeper" >> {
      val assertionData = "Steer the wheel"
      zk.create("/path4", "".getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
      Await.result(instance.addPathToCache("/path4"), 30.seconds)
      eventually {
        instance.getChildren("/path4") must haveSize(0)
      }

      val stat = Option(zk.exists("/path4", false))
      stat must not be (None)
      zk.setData("/path4", assertionData.getBytes, stat.get.getVersion)

      eventually {
        val data = new String(instance.getData("/path4"))
        data must beEqualTo(assertionData)
      }
    }

    "synchronize the data of children of defined znodes" >> {
      var data = ""
      val assertionData = "Steer"
      zk.create("/path5", data.getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
      Await.result(instance.addPathToCache("/path5"), 30.seconds)
      eventually {
        instance.getChildren("/path5") must haveSize(0)
      }

      zk.create("/path5/child1", data.getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
      zk.create("/path5/child1/child2", data.getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)

      val stat = Option(zk.exists("/path5/child1/child2", false))
      zk.setData("/path5/child1/child2", assertionData.getBytes, stat.get.getVersion)

      eventually(100, 100.millis) {
        data = new String(instance.getData("/path5/child1/child2"))
        data must beEqualTo(assertionData)
      }
    }

    "remove a path from synchronizing from the ZooKeeper" >> {
      // Create the structure /path6/child1/child2
      zk.create("/path6", "".getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
      Await.result(instance.addPathToCache("/path6"), 30.seconds)
      eventually {
        instance.getChildren("/path6") must haveSize(0)
      }
      zk.create("/path6/child1", "".getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
      eventually {
        instance.getChildren("/path6") must haveSize(1)
      }
      zk.create("/path6/child1/child2", "".getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
      eventually {
        instance.getChildren("/path6/child1") must haveSize(1)
      }

      Await.result(instance.removePathFromCache("/path6"), 30.seconds)
      instance.getChildren("/path6") must throwA[NotCachedException]

      eventually {
        instance.getChildren("/path6/child1") must throwA[NotCachedException]
        instance.getChildren("/path6/child1/child2") must throwA[NotCachedException]
      }

      // Create one more child
      zk.create("/path6/child1/child2/child3", "".getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
      Thread.sleep(500)
      instance.getChildren("/path6/child1/child2/child3") must throwA[NotCachedException]
    }

    "update itself when a node is deleted from the ZooKeeper" >> {
      zk.create("/path7", "".getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
      Await.result(instance.addPathToCache("/path7"), 30.seconds)
      eventually {
        instance.getChildren("/path7") must haveSize(0)
      }
      zk.create("/path7/child1", "".getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
      eventually {
        instance.getChildren("/path7") must haveSize(1)
      }

      val stat = Option(zk.exists("/path7/child1", false))
      stat must not be (None)
      zk.delete("/path7/child1", stat.get.getVersion)
      eventually {
        instance.getChildren("/path7") must haveSize(0)
      }
    }

    "update itself when a parent of a watched node is deleted from the ZooKeeper" >> {
      zk.create("/path8", "".getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
      Await.result(instance.addPathToCache("/path8"), 30.seconds)
      eventually {
        instance.getChildren("/path8") must haveSize(0)
      }
      zk.create("/path8/child1", "".getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
      eventually {
        instance.getChildren("/path8") must haveSize(1)
      }

      val stat1 = Option(zk.exists("/path8/child1", false))
      stat1 must not be (None)
      zk.delete("/path8/child1", stat1.get.getVersion)
      val stat2 = Option(zk.exists("/path8", false))
      stat2 must not be (None)
      zk.delete("/path8", stat2.get.getVersion)
      eventually(100, 100.millis) {
        instance.getChildren("/path8") must throwA[NotCachedException]
      }
    }

    "handle the case when a path that is added to the cache gets later deleted from the ZooKeeper and then it is recreated" >> {
      zk.create("/path9", "".getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
      // Add the path to the cache
      Await.result(instance.addPathToCache("/path9"), 30.seconds)
      eventually {
        instance.getChildren("/path9") must haveSize(0)
      }

      // Delete the added-to-cache path from the ZooKeeper, without calling the removePathFromCache for it
      val stat = Option(zk.exists("/path9", false))
      stat must not be (None)
      zk.delete("/path9", stat.get.getVersion)

      eventually {
        instance.getChildren("/path9") must throwA[NotCachedException]
      }
      // Add the path to the ZooKeeper again
      zk.create("/path9", "".getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
      // Assert the existence in the cache
      eventually {
        instance.getChildren("/path9") must haveSize(0)
      }
    }
  }
}