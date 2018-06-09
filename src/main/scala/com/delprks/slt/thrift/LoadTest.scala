package com.delprks.slt.thrift

import java.util.concurrent.Executors

import scala.collection.mutable.ListBuffer
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.{ExecutionContext, Future}

object LoadTest extends App with LoadGenerator {
  val invocations = args(0).toInt
  val parallelism = args(1).toInt
  val duration = args(2).toInt

  println(s"Invoking long running method $invocations times - or maximum $duration seconds")

  implicit val context: ExecutionContext =
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(parallelism * 20))

  def longRunningMethod(): Future[String] = {
    Thread.sleep(4)

    Future.successful("Method finished running")
  }

  val testStartTime = System.currentTimeMillis()
  val testEndTime = testStartTime + duration * 1000

  val parallelInvocation = (1 to invocations).toParArray

  parallelInvocation.tasksupport = new ForkJoinTaskSupport(new java.util.concurrent.ForkJoinPool(parallelism))

  val results: ListBuffer[Future[Long]] = execute()

  def execute(): ListBuffer[Future[Long]] = {
    var i = new ListBuffer[Future[Long]]

    parallelInvocation.par.foreach { _ =>
      i += measure(longRunningMethod())

      if (System.currentTimeMillis() >= testEndTime) {
        return i
      }
    }

    i
  }

  val testDuration: Double = (System.currentTimeMillis() - testStartTime).toDouble

  case class TestResult(min: Long, max: Long, average: Double)

  val testResultsF = Future.sequence(results).map { response =>
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
