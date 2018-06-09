package com.delprks.thrift

import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.parallel._
import scala.collection.parallel.ForkJoinTaskSupport
import scala.collection.parallel.mutable.ParArray

object LoadTest extends App with Timer {
  val invocations = args(0).toInt
  val parallelism = args(1).toInt

  println(s"Invoking long running method $invocations times")

  implicit val context: ExecutionContext =
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(parallelism * 20))

  def longRunningMethod(): Future[String] = {
    Thread.sleep(20)

    Future.successful("Method finished running")
  }

  val testStartTime = System.currentTimeMillis()

  val parallelInvocation = (1 to invocations).toParArray

  parallelInvocation.tasksupport = new ForkJoinTaskSupport(
    new java.util.concurrent.ForkJoinPool(parallelism))

  val results: ParArray[Future[Long]] = parallelInvocation.map(_ => measure(longRunningMethod()))

  val testDuration: Double = (System.currentTimeMillis() - testStartTime).toDouble

  val parArray: Seq[Future[Long]] = results.map {
    _.map(r => r)
  }.seq

  val responses: Future[Seq[Long]] = Future.sequence(parArray)

  case class TestResult(min: Long, max: Long, average: Double)

  val testResultsF = responses.map { response =>
    val min = response.min
    val max = response.max
    val average = truncateAt(avg(response), 2)

    TestResult(min, max, average)
  }

  println(s"Running time: ${truncateAt(testDuration / 1000, 2)} seconds (${rps(testDuration.toDouble, invocations)}rps)")

  testResultsF.map { response =>
    println(s"Min: ${response.min}ms")
    println(s"Max: ${response.max}ms")
    println(s"Average: ${response.average}ms")
    System.exit(0)
  }

}
