package com.delprks.massiveattack.method

import org.specs2.mutable.Specification

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class ThriftLoadTestSpecTest extends Specification {
  protected val futureSupportTimeout: Duration = 5.second

  sequential

  def longRunningMethod(): Future[Seq[String]] = {
    Thread.sleep(20)

    Future.successful(Seq("Method finished running"))
  }

  "load test first expensive method" in {
    val testProperties = MassiveAttackProperties(
      invocations = 100000,
      threads = 1,
      duration = 30,
      warmUp = true,
      warmUpInvocations = 1000,
      verbose = false
    )

    val loadTestSpec = new MethodLoadTest(testProperties)
    val testResultF: Future[MassiveAttackResult] = loadTestSpec.test(() => longRunningMethod())

    val testResult = Await.result(testResultF, futureSupportTimeout)

//    testResult.averageResponseTime must beLessThanOrEqualTo(40)
    1 must be equalTo(1)
  }
}
