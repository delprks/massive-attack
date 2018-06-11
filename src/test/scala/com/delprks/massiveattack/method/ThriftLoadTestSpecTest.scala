package com.delprks.massiveattack.method

import org.specs2.mutable.Specification

import scala.concurrent.Future

class ThriftLoadTestSpecTest extends Specification {
  def longRunningMethod(): Future[Seq[String]] = {
    Thread.sleep(20)

    Future.successful(Seq("Method finished running"))
  }

  "PlayableItemMapper" should {
    "load test first expensive method" in {
      val loadTestSpec = new MethodLoadTest(threads = 600, invocations = 5000000, duration = 20)

      loadTestSpec.test(() => longRunningMethod())

      1 shouldEqual (1)
    }
  }
}
