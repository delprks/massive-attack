package com.delprks.massiveattack.method

import com.delprks.massiveattack.method.result.MethodPerformanceResult
import org.specs2.mutable.Specification

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class MethodPerformanceSpec extends Specification {
  protected lazy val futureSupportTimeout: Duration = 2.minutes

  sequential

  def longRunningMethod(): Future[String] = {
    Thread.sleep(20)

    Future.successful("Method finished running")
  }

  "long running method should have average response times of less than 40ms" in {
    val testProperties = MethodPerformanceProps(
      warmUp = false,
      invocations = 10000,
      threads = 2,
      duration = 30
    )

    val methodPerformance = new MethodPerformance(testProperties)

    val testResultF: Future[MethodPerformanceResult] = methodPerformance.measure(() => longRunningMethod())
    val testResult = Await.result(testResultF, futureSupportTimeout)

    testResult.averageResponseTime must beLessThanOrEqualTo(40)
  }
}
