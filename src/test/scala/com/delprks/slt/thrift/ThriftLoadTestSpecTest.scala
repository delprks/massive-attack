package com.delprks.slt.thrift

import org.specs2.mutable.Specification

import scala.concurrent.Future

class ThriftLoadTestSpecTest extends Specification {
  def longRunningMethod(): Future[String] = {
    Thread.sleep(20)

    Future.successful("Method finished running")
  }

  "PlayableItemMapper" should {
    "load test first expensive method" in {
      val loadTestSpec = new ThriftLoadTest(threads = 800, invocations = 5000000, duration = 60)

      loadTestSpec.test(() => longRunningMethod())

      1 shouldEqual (1)
    }
  }
}
