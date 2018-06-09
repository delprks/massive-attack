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
      val loadTestSpec = new ThriftLoadTest(threads = 600, invocations = 50000)

      loadTestSpec.test(() => longRunningMethod())

      1 shouldEqual (1)
    }
  }
}
