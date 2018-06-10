package com.delprks.slt.thrift

import java.util.concurrent.Executors

import scala.collection.mutable.ListBuffer
import scala.collection.parallel.ForkJoinTaskSupport
import scala.collection.parallel.mutable.ParArray
import scala.concurrent.ExecutionContext
import com.twitter.util.{Future => TwitterFuture, Promise => TwitterPromise, Return, Throw}
import scala.concurrent.{Future => ScalaFuture, Promise => ScalaPromise, ExecutionContext}
import scala.util.{Success, Failure}

class ThriftLoadTest(
  invocations: Int = 10000,
  threads: Int = 20,
  duration: Int = 30,
  warmUp: Boolean = true,
  warmUpInvocations: Int = 100
) extends LoadGenerator {

  private implicit val context: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(threads))

  def asScala[A](tf: TwitterFuture[A])(implicit e: ExecutionContext): ScalaFuture[A] = {
    val promise: ScalaPromise[A] = ScalaPromise()

    tf.respond {
      case Return(value) => promise.success(value)
      case Throw(exception) => promise.failure(exception)
    }

    promise.future
  }

  def test(longRunningMethod: () => ScalaFuture[_]) = {
    if (warmUp) {
      println(s"Warming up the JVM...")

      (1 to warmUpInvocations).foreach { _ =>
        longRunningMethod()
      }
    }

    println(s"Invoking long running method $invocations times - or maximum $duration seconds")

    val testStartTime = System.currentTimeMillis()
    val testEndTime = testStartTime + duration * 1000

    val parallelInvocation: ParArray[Int] = (1 to invocations).toParArray

    parallelInvocation.tasksupport = new ForkJoinTaskSupport(new java.util.concurrent.ForkJoinPool(threads))

    val results: ListBuffer[ScalaFuture[Long]] = execute(parallelInvocation, () => longRunningMethod(), testEndTime)

    val testDuration: Double = (System.currentTimeMillis() - testStartTime).toDouble

    case class TestResult(min: Long, max: Long, average: Double)

    val testResultsF = ScalaFuture.sequence(results).map { response =>
      val average = truncateAt(avg(response), 2)

      TestResult(response.min, response.max, average)
    }

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

  def execute(parallelInvocation: ParArray[Int], longRunningMethod: () => ScalaFuture[Any], testEndTime: Long): ListBuffer[ScalaFuture[Long]] = {
    var i = new ListBuffer[ScalaFuture[Long]]

    parallelInvocation.par.foreach { _ =>
      i += measure(longRunningMethod())

      if (System.currentTimeMillis() >= testEndTime) {
        return i
      }
    }

    i
  }
}
