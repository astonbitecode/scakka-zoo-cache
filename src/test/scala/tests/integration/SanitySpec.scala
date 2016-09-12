package tests.integration

import org.junit.runner.RunWith
import org.specs2.mutable
import org.specs2.runner.JUnitRunner
import org.specs2.mock.Mockito
import org.specs2.specification.BeforeEach
import org.apache.zookeeper.{
  ZooKeeper,
  KeeperException,
  ZooDefs,
  CreateMode
}
import org.apache.curator.test.TestingServer
import com.github.astonbitecode.api.scala.ScakkaZooCache
import scala.concurrent.Await
import scala.concurrent.duration._
import java.util.ArrayList
import org.apache.zookeeper.data.ACL
import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class SanitySpec extends mutable.Specification with Mockito with BeforeEach {
  val server = new TestingServer(true)
  var zk: ZooKeeper = new ZooKeeper(server.getConnectString, 1000, null)
  var instance: ScakkaZooCache = ScakkaZooCache.scala(zk)

  override def before() {
  }

  sequential

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

    "synchronize the data of a znode from the ZooKeeper when other Threads are changing the same path" >> {
      val assertionData1 = "Steer"
      val assertionData2 = "the wheel"
      zk.create("/path5", "".getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
      Await.result(instance.addPathToCache("/path5"), 30.seconds)
      eventually {
        instance.getChildren("/path5") must haveSize(0)
      }

      val stat = Option(zk.exists("/path5", false))
      stat must not be (None)
      zk.setData("/path5", "".getBytes, stat.get.getVersion)

      // Spawn two Futures that are updating continuously
      Future {
        for (_ <- 0 until 1000) {
          val stat = Option(zk.exists("/path5", false))
          zk.setData("/path5", assertionData1.getBytes, stat.get.getVersion)
        }
      }
      Future {
        for (_ <- 0 until 1000) {
          val stat = Option(zk.exists("/path5", false))
          zk.setData("/path5", assertionData2.getBytes, stat.get.getVersion)
        }
      }

      eventually {
        val data = new String(instance.getData("/path5"))
        data must beEqualTo(assertionData1) or beEqualTo(assertionData2)
      }
    }

  }
}