package com.delprks.massiveattack.method

import java.util.concurrent.Executors

import scala.collection.mutable.ListBuffer
import scala.collection.parallel.ForkJoinTaskSupport
import scala.collection.parallel.mutable.ParArray
import com.twitter.util.{Future => TwitterFuture, Return, Throw}

import scala.concurrent.{Future => ScalaFuture, Promise => ScalaPromise, ExecutionContext}

class MethodLoadTest(
  invocations: Int = 10000,
  threads: Int = 20,
  duration: Int = 30,
  warmUp: Boolean = true,
  warmUpInvocations: Int = 100
) extends LoadGenerator {

  private implicit val context: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(threads))

  def test(longRunningMethod: () => TwitterFuture[_]) = {
    if (warmUp) {
      println(s"Warming up the JVM...")

      warmUpTwitterMethod(longRunningMethod)
    }

    println(s"Invoking long running method $invocations times - or maximum $duration seconds")

    val testStartTime = System.currentTimeMillis()
    val testEndTime = testStartTime + duration * 1000

    val parallelInvocation: ParArray[Int] = (1 to invocations).toParArray

    parallelInvocation.tasksupport = new ForkJoinTaskSupport(new java.util.concurrent.ForkJoinPool(threads))

    val results: ListBuffer[TwitterFuture[Long]] = executeTwitterMethod(parallelInvocation, () => longRunningMethod(), testEndTime)

    val testDuration: Double = (System.currentTimeMillis() - testStartTime).toDouble

    val testResultsF: TwitterFuture[TestResult] = twitterTestResults(results)

    testResultsF.map { response =>
      println(s"Min: ${response.min}ms")
      println(s"Max: ${response.max}ms")
      println(s"Average: ${response.average}ms")
      println(s"Invocations: ${results.size}")
      println(s"Duration: ${truncateAt(testDuration / 1000, 2)}s")
      println(s"Requests: ${rps(testDuration.toDouble, results.size)}rps")

      System.exit(0)
    }
  }

  def test(longRunningMethod: () => ScalaFuture[_]) = {
    if (warmUp) {
      println(s"Warming up the JVM...")

      warmUpScalaMethod(longRunningMethod)
    }

    println(s"Invoking long running method $invocations times - or maximum $duration seconds")

    val testStartTime = System.currentTimeMillis()
    val testEndTime = testStartTime + duration * 1000

    val parallelInvocation: ParArray[Int] = (1 to invocations).toParArray

    parallelInvocation.tasksupport = new ForkJoinTaskSupport(new java.util.concurrent.ForkJoinPool(threads))

    val results: ListBuffer[ScalaFuture[Long]] = executeScalaMethod(parallelInvocation, () => longRunningMethod(), testEndTime)

    val testDuration: Double = (System.currentTimeMillis() - testStartTime).toDouble

    val testResultsF: ScalaFuture[TestResult] = scalaTestResults(results)

    testResultsF.map { response =>
      println(s"Min: ${response.min}ms")
      println(s"Max: ${response.max}ms")
      println(s"Average: ${response.average}ms")
      println(s"Invocations: ${results.size}")
      println(s"Duration: ${truncateAt(testDuration / 1000, 2)}s")
      println(s"Requests: ${rps(testDuration.toDouble, results.size)}rps")

      System.exit(0)
    }

  }

  private def scalaTestResults(results: ListBuffer[ScalaFuture[Long]]): ScalaFuture[TestResult] = ScalaFuture.sequence(results).map { response =>
    val average = truncateAt(avg(response), 2)

    TestResult(response.min, response.max, average)
  }

  private def twitterTestResults(results: ListBuffer[TwitterFuture[Long]]): TwitterFuture[TestResult] = TwitterFuture.collect(results).map { response =>
    val average = truncateAt(avg(response), 2)

    TestResult(response.min, response.max, average)
  }

  private def warmUpScalaMethod(longRunningMethod: () => ScalaFuture[_]) = (1 to warmUpInvocations).foreach(_ => longRunningMethod())

  private def warmUpTwitterMethod(longRunningMethod: () => TwitterFuture[_]) = (1 to warmUpInvocations).foreach(_ => longRunningMethod())

  def executeScalaMethod(parallelInvocation: ParArray[Int], longRunningMethod: () => ScalaFuture[Any], testEndTime: Long): ListBuffer[ScalaFuture[Long]] = {
    var testResult = new ListBuffer[ScalaFuture[Long]]

    parallelInvocation.par.foreach { _ =>
      testResult += measure(longRunningMethod())

      if (System.currentTimeMillis() >= testEndTime) {
        return testResult
      }
    }

    testResult
  }

  def executeTwitterMethod(parallelInvocation: ParArray[Int], longRunningMethod: () => TwitterFuture[Any], testEndTime: Long): ListBuffer[TwitterFuture[Long]] = {
    var testResult = new ListBuffer[TwitterFuture[Long]]

    parallelInvocation.par.foreach { _ =>
      testResult += measure(longRunningMethod())

      if (System.currentTimeMillis() >= testEndTime) {
        return testResult
      }
    }

    testResult
  }
}
