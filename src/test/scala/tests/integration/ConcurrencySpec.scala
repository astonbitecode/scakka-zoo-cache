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
import scala.concurrent.Await
import scala.concurrent.duration._
import java.util.ArrayList
import org.apache.zookeeper.data.ACL
import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.collection.mutable.HashSet
import org.specs2.specification.AfterAll
import com.github.astonbitecode.zoocache.ScakkaZooCacheFactory

@RunWith(classOf[JUnitRunner])
class ConcurrencySpec extends mutable.Specification with AfterAll {
  val server = new TestingServer(true)
  val zk1: ZooKeeper = new ZooKeeper(server.getConnectString, 1000, null)
  val zk2: ZooKeeper = new ZooKeeper(server.getConnectString, 1000, null)
  val zk3: ZooKeeper = new ZooKeeper(server.getConnectString, 1000, null)
  val instance = ScakkaZooCacheFactory.scala(zk3)
  val iterations = 100000

  override def afterAll() {
    Await.result(instance.stop(), 30.seconds)
    zk1.close()
    zk2.close()
    zk3.close()
    server.close()
  }

  sequential

  "Integration test - Concurrency".txt

  "No problems should exist " >> {
    "when ZooKeeper is updated concurrently by many threads" >> {
      // Create the path data
      val pathData = "Path Data"
      zk3.create("/this", "".getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
      zk3.create("/this/is", "".getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
      zk3.create("/this/is/a", "".getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
      zk3.create("/this/is/a/path", pathData.getBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL)
      // Register the path to the cache
      Await.result(instance.addPathToCache("/this/is/a/path"), 30.seconds)

      // Update the data concurrently
      var f1Spawned = false
      var f1DoneAtLeastOnce = false
      Future {
        f1Spawned = true
        println("---Spawned future 1")
        for (i <- 0 to iterations) {
          try {
            val stat = Option(zk1.exists("/this/is/a/path", false))
            stat must not be (None)
            zk1.setData("/this/is/a/path", "User1".getBytes, stat.get.getVersion)
            if(!f1DoneAtLeastOnce) {
              println("Future 1 updated at least once")
            }
            f1DoneAtLeastOnce = true
          } catch {
            case error: Throwable => // ignore //println("error on User1 for " + i)
          }
        }
      }
      var f2Spawned = false
      var f2DoneAtLeastOnce = false
      Future {
        f2Spawned = true
        println("---Spawned future 2")
        for (i <- 0 to iterations) {
          try {
            val stat = Option(zk2.exists("/this/is/a/path", false))
            stat must not be (None)
            zk2.setData("/this/is/a/path", "User2".getBytes, stat.get.getVersion)
            if(!f2DoneAtLeastOnce) {
              println("Future 2 updated at least once")
            }
            f2DoneAtLeastOnce = true
          } catch {
            case error: Throwable => // ignore //println("error on User2 for " + i)
          }
        }
      }

      eventually {
        instance.getChildren("/this/is/a/path") must haveSize(0)
      }

      var f3Spawned = false
      var set = HashSet.empty[String]
      Future {
        f3Spawned = true
        while(true) {
          val sizeBefore = set.size
          val data = instance.getData("/this/is/a/path")
          set.add(new String(data))
          if (sizeBefore < set.size) {
            println(set)
          }
          Thread.sleep(50)
        }
      }

      eventually {
        f1Spawned & f2Spawned & f3Spawned must beEqualTo(true)
      }
      eventually {
        set.size must beGreaterThanOrEqualTo(2)
      }
    }
  }
}