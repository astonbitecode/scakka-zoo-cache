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
import com.github.astonbitecode.ScakkaZooCache
import scala.concurrent.Await
import scala.concurrent.duration._
import java.util.ArrayList
import org.apache.zookeeper.data.ACL
import scala.collection.JavaConversions._

@RunWith(classOf[JUnitRunner])
class SanitySpec extends mutable.Specification with Mockito with BeforeEach {
  val server = new TestingServer(true)
  var zk: ZooKeeper = _
  var instance: ScakkaZooCache = _

  override def before() {
    zk = new ZooKeeper(server.getConnectString, 1000, null)
    instance = ScakkaZooCache(zk)
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
  }
}