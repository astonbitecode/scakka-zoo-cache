package com.github.astonbitecode.zoocache.impl.java

import org.junit.runner.RunWith
import org.specs2.mutable
import org.specs2.runner.JUnitRunner
import org.specs2.mock.Mockito
import com.github.astonbitecode.zoocache.api.scala.ScakkaZooCache
import com.github.astonbitecode.zoocache.api.ScakkaException.NotCachedException
import com.github.astonbitecode.zoocache.CacheUpdaterActor.ZkNodeElement
import com.github.astonbitecode.zoocache.Internals.implicits._
import com.github.astonbitecode.zoocache.api.dtos.CacheResult

@RunWith(classOf[JUnitRunner])
class ScakkaZooCacheJavaImplSpec extends mutable.Specification with Mockito {

  "Tests for the Java implementation of the ScakkaZooCache ".txt

  "Get Children " >> {
    "on a non-existing node should " >> {
      "fail" >> {
        val scalaCache = mock[ScakkaZooCache]
        scalaCache.getChildren(any).throws(new NotCachedException(""))
        val instance = new ScakkaZooCacheJavaImpl(scalaCache)
        instance.getChildren("/") must throwA[NotCachedException]
      }
    }

    " on a an existing node " >> {
      "with no children should return an empty Java List" >> {
        val scalaCache = mock[ScakkaZooCache]
        scalaCache.getChildren(any).returns(List.empty)
        val instance = new ScakkaZooCacheJavaImpl(scalaCache)
        instance.getChildren("/path") must beAnInstanceOf[java.util.List[java.lang.String]]
        instance.getChildren("/path") must haveSize(0)
      }

      "with children should return a non empty Java List" >> {
        val scalaCache = mock[ScakkaZooCache]
        scalaCache.getChildren(any).returns(List("child1", "child2", "child3"))
        val instance = new ScakkaZooCacheJavaImpl(scalaCache)
        instance.getChildren("/path") must beAnInstanceOf[java.util.List[java.lang.String]]
        instance.getChildren("/path") must haveSize(3)
      }
    }
  }

  "Get Data " >> {
    "on a non-existing node should " >> {
      "fail" >> {
        val scalaCache = mock[ScakkaZooCache]
        scalaCache.getData(any).throws(new NotCachedException(""))
        val instance = new ScakkaZooCacheJavaImpl(scalaCache)
        instance.getData("/path") must throwA[NotCachedException]
      }
    }

    " on a an existing node " >> {
      "with no data should return an empty result" >> {
        val scalaCache = mock[ScakkaZooCache]
        scalaCache.getData(any).returns("v".getBytes)
        val instance = new ScakkaZooCacheJavaImpl(scalaCache)
        instance.getData("/path") must equalTo("v".getBytes)
      }

      "with data should return the data of the node" >> {
        val scalaCache = mock[ScakkaZooCache]
        scalaCache.getData(any).returns("v".getBytes)
        val instance = new ScakkaZooCacheJavaImpl(scalaCache)
        instance.getData("/path") must equalTo("v".getBytes)
      }
    }
  }

  "Add Path to Cache " >> {
    "should return a Java Future" >> {
      val scalaCache = mock[ScakkaZooCache]
      val instance = new ScakkaZooCacheJavaImpl(scalaCache)
      instance.addPathToCache("/non/existing/path") must beAnInstanceOf[java.util.concurrent.Future[Unit]]
    }
  }

  "Remove Path from Cache " >> {
    "should return a Java Future" >> {
      val scalaCache = mock[ScakkaZooCache]
      val instance = new ScakkaZooCacheJavaImpl(scalaCache)
      instance.removePathFromCache("/non/existing/path") must beAnInstanceOf[java.util.concurrent.Future[Unit]]
    }
  }

  "Shutdown should " >> {
    "should return a Java Future" >> {
      val scalaCache = mock[ScakkaZooCache]
      val instance = new ScakkaZooCacheJavaImpl(scalaCache)
      instance.stop() must beAnInstanceOf[java.util.concurrent.Future[Unit]]
    }
  }

  "Find should " >> {
    "return results" >> {
      val scalaCache = mock[ScakkaZooCache]
      val cr = CacheResult("/a/path", "data".getBytes, List("1", "2"))
      scalaCache.find(any).returns(List(cr))
      val instance = new ScakkaZooCacheJavaImpl(scalaCache)

      val results = instance.find("(^\\/a\\/path\\/[\\w]*)")
      results must haveSize(1)
    }
  }
}