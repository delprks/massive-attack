package com.delprks.thrift

import scala.concurrent.Future

object LoadTest extends App with Timer {
  val invocations = args(0).toInt

  println(s"Invoking long running method $invocations times")

  import scala.concurrent.ExecutionContext.Implicits.global

  def longRunningMethod(): Future[String] = {
    Thread.sleep(500)

    Future.successful("Method finished running")
  }

  val testStartTime = System.currentTimeMillis()

  val results: Seq[Future[Long]] = (1 to invocations).map(_ => measure(longRunningMethod()))

  results.foreach {
    _.map(result => println(s"$result milliseconds"))
  }

  val testDuration = System.currentTimeMillis() - testStartTime

  println(s"It took ${testDuration / 1000}s to run the test")
}
