package com.delprks.massiveattack.method

import java.util.concurrent.Executors

import scala.collection.mutable.ListBuffer
import scala.collection.parallel.ForkJoinTaskSupport
import scala.collection.parallel.mutable.ParArray
import com.twitter.util.{Future => TwitterFuture}

import scala.concurrent.{Future => ScalaFuture, ExecutionContext}

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

    val results: ListBuffer[TwitterFuture[MeasureResult]] = measureTwitterMethod(parallelInvocation, () => longRunningMethod(), testEndTime)

    val testDuration: Double = (System.currentTimeMillis() - testStartTime).toDouble

    val testResultsF: TwitterFuture[TestResult] = twitterTestResults(results)

    testResultsF.map { response =>
      println(s"Min: ${response.min}ms")
      println(s"Max: ${response.max}ms")
      println(s"Average: ${response.average}ms")
      println(s"Invocations: ${results.size}")
      println(s"Duration: ${truncateAt(testDuration / 1000, 2)}s")
      println(s"Average requests: ${response.rpsAvg}rps")
      println(s"Min requests: ${response.rpsMin}rps")
      println(s"Max requests: ${response.rpsMax}rps")

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

    val results: ListBuffer[ScalaFuture[MeasureResult]] = measureScalaMethod(parallelInvocation, () => longRunningMethod(), testEndTime)

    val testDuration: Double = (System.currentTimeMillis() - testStartTime).toDouble

    val testResultsF: ScalaFuture[TestResult] = scalaTestResults(results)

    testResultsF.map { response =>
      println(s"Min: ${response.min}ms")
      println(s"Max: ${response.max}ms")
      println(s"Average: ${response.average}ms")
      println(s"Invocations: ${results.size}")
      println(s"Duration: ${truncateAt(testDuration / 1000, 2)}s")
      println(s"Average requests: ${response.rpsAvg}rps")
      println(s"Min requests: ${response.rpsMin}rps")
      println(s"Max requests: ${response.rpsMax}rps")

      System.exit(0)
    }

  }

  private def scalaTestResults(results: ListBuffer[ScalaFuture[MeasureResult]]): ScalaFuture[TestResult] = ScalaFuture.sequence(results).map { response =>
    val average = truncateAt(avg(response.map(_.duration)), 2)
    val invocationSeconds = response.map(_.endTime / 1000)
    val requestTimesPerSecond = invocationSeconds.groupBy(identity).map(_._2.size)

    val rpsAverage = truncateAt(avg(requestTimesPerSecond.map(_.toLong).toSeq), 2)

    TestResult(response.map(_.duration).min, response.map(_.duration).max, average, requestTimesPerSecond.min, requestTimesPerSecond.max, rpsAverage)
  }

  private def twitterTestResults(results: ListBuffer[TwitterFuture[MeasureResult]]): TwitterFuture[TestResult] = TwitterFuture.collect(results).map { response =>
    val average = truncateAt(avg(response.map(_.duration)), 2)
    val invocationSeconds = response.map(_.endTime / 1000)
    val requestTimesPerSecond = invocationSeconds.groupBy(identity).map(_._2.size)

    val rpsAverage = truncateAt(avg(requestTimesPerSecond.map(_.toLong).toSeq), 2)

    TestResult(response.map(_.duration).min, response.map(_.duration).max, average, requestTimesPerSecond.min, requestTimesPerSecond.max, rpsAverage)
  }

  private def warmUpScalaMethod(longRunningMethod: () => ScalaFuture[_]) = (1 to warmUpInvocations).foreach(_ => longRunningMethod())

  private def warmUpTwitterMethod(longRunningMethod: () => TwitterFuture[_]) = (1 to warmUpInvocations).foreach(_ => longRunningMethod())

  def measureScalaMethod(parallelInvocation: ParArray[Int], longRunningMethod: () => ScalaFuture[Any], testEndTime: Long): ListBuffer[ScalaFuture[MeasureResult]] = {
    var testResult = new ListBuffer[ScalaFuture[MeasureResult]]

    parallelInvocation.par.foreach { _ =>
      testResult += measure(longRunningMethod())

      if (System.currentTimeMillis() >= testEndTime) {
        return testResult
      }
    }

    testResult
  }

  def measureTwitterMethod(parallelInvocation: ParArray[Int], longRunningMethod: () => TwitterFuture[Any], testEndTime: Long): ListBuffer[TwitterFuture[MeasureResult]] = {
    var testResult = new ListBuffer[TwitterFuture[MeasureResult]]

    parallelInvocation.par.foreach { _ =>
      testResult += measure(longRunningMethod())

      if (System.currentTimeMillis() >= testEndTime) {
        return testResult
      }
    }

    testResult
  }
}
