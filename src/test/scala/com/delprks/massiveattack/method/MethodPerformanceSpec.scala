package com.delprks.massiveattack.method

import com.delprks.massiveattack.method.result.MethodPerformanceResult
import org.specs2.mutable.Specification

import scala.concurrent.duration._
import scala.concurrent.{Await, Future => ScalaFuture}
import com.twitter.util.{Future => TwitterFuture}

class MethodPerformanceSpec extends Specification {
  protected lazy val futureSupportTimeout: Duration = 30.seconds

  sequential

  private def longRunningMethodWithScalaFuture(): ScalaFuture[String] = {
    Thread.sleep(20)

    ScalaFuture.successful("Method finished running")
  }

  private def longRunningMethodWithTwitterFuture(): TwitterFuture[String] = {
    Thread.sleep(20)

    TwitterFuture.value("Method finished running")
  }

  "long running method that returns a Scala Future should have average response times of less than 40ms" in {
    val testProperties = MethodPerformanceProps(
      warmUp = false,
      invocations = 2000000,
      threads = 50,
      duration = 20
    )

    val methodPerformance = new MethodPerformance(testProperties)

    val testResultF: ScalaFuture[MethodPerformanceResult] = methodPerformance.measure(() => longRunningMethodWithScalaFuture())
    val testResult = Await.result(testResultF, futureSupportTimeout)

    testResult.averageResponseTime must beLessThanOrEqualTo(40)
  }

  "long running method that returns a Twitter Future should have average response times of less than 40ms" in {
    val testProperties = MethodPerformanceProps(
      warmUp = false,
      invocations = 2000000,
      threads = 50,
      duration = 20
    )

    val methodPerformance = new MethodPerformance(testProperties)

    val testResultF: ScalaFuture[MethodPerformanceResult] = methodPerformance.measure(() => longRunningMethodWithScalaFuture())
    val testResult = Await.result(testResultF, futureSupportTimeout)

    testResult.averageResponseTime must beLessThanOrEqualTo(40)
  }
}
