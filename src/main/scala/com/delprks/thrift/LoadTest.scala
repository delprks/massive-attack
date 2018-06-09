package com.delprks.thrift

import scala.concurrent.Future
import scala.collection.parallel._
import scala.collection.parallel.ForkJoinTaskSupport
import scala.collection.parallel.mutable.ParArray

object LoadTest extends App with Timer {
  val invocations = args(0).toInt
  val parallelism = args(1).toInt

  println(s"Invoking long running method $invocations times")

  import scala.concurrent.ExecutionContext.Implicits.global

  def longRunningMethod(): Future[String] = {
    Thread.sleep(500)

    Future.successful("Method finished running")
  }

  val testStartTime = System.currentTimeMillis()

  val parallelInvocation = (1 to invocations).toParArray

  parallelInvocation.tasksupport = new ForkJoinTaskSupport(
    new java.util.concurrent.ForkJoinPool(parallelism))

  val results: ParArray[Future[Long]] = parallelInvocation.map(_ => measure(longRunningMethod()))

  results.foreach {
    _.map(result => println(s"$result milliseconds"))
  }

  val testDuration = System.currentTimeMillis() - testStartTime

  println(s"It took ${testDuration / 1000}s to run the test")
}
