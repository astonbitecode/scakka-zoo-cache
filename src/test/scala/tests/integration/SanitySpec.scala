package tests.integration

import org.junit.runner.RunWith
import org.specs2.mutable
import org.specs2.runner.JUnitRunner
import org.specs2.mock.Mockito
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
class SanitySpec extends mutable.Specification with Mockito {
  val server = new TestingServer(true)
  val zk = new ZooKeeper(server.getConnectString, 1000, null)

  "Integration test - Sanity ".txt

  "Initialization should succeed" >> {
    val instance = ScakkaZooCache(zk)
    zk.create("/path1", "".getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL)
    Await.result(instance.addPathToCache("/path1"), 30.seconds)
    eventually {
      instance.getChildren("/path1") must haveSize(0)
    }
  }
}