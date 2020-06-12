package bbc.rms.massiveattack.method

import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import scala.concurrent.{Await, Future => ScalaFuture}
import com.twitter.util.{Future => TwitterFuture}
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import bbc.rms.massiveattack.method.result.MethodPerformanceResult
import org.scalatest.wordspec.AnyWordSpecLike

class MethodPerformanceSpec extends TestKit(ActorSystem("MassiveAttackSpec")) with ImplicitSender
  with AnyWordSpecLike with Matchers with BeforeAndAfterAll {

  protected lazy val futureSupportTimeout: Duration = 30.seconds

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

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
      invocations = 10000,
      threads = 50,
      duration = 35,
      report = true,
      reportName = Some("scala_future_performance_test")
    )

    val methodPerformance = new MethodPerformance(testProperties)

    val testResultF: ScalaFuture[MethodPerformanceResult] = methodPerformance.measure(() => longRunningMethodWithScalaFuture())
    val testResult = Await.result(testResultF, futureSupportTimeout)

    testResult.responseTimeAvg should be < 40
  }

  "long running method that returns a Twitter Future should have average response times of less than 40ms" in {
    val testProperties = MethodPerformanceProps(
      invocations = 10000,
      threads = 50,
      duration = 35,
      report = true,
      reportName = Some("twitter_future_performance_test")
    )

    val methodPerformance = new MethodPerformance(testProperties)

    val testResultF: ScalaFuture[MethodPerformanceResult] = methodPerformance.measure(() => longRunningMethodWithTwitterFuture())
    val testResult = Await.result(testResultF, futureSupportTimeout)

    testResult.responseTimeAvg should be < 40
  }
}
