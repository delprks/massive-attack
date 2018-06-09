package com.delprks.thrift

import scala.concurrent.Future

trait Timer {
  import scala.concurrent.ExecutionContext.Implicits.global

  def measure(method: => Future[Any]): Future[Long] = {
    val currentTime = System.currentTimeMillis()

    method map { _ =>
      val timeAfterExecution = System.currentTimeMillis()

      timeAfterExecution - currentTime
    }
  }

  def avg(s: Seq[Long]): Double = s.foldLeft((0.0, 1)) ((acc, i) => (acc._1 + (i - acc._1) / acc._2, acc._2 + 1))._1

  def truncateAt(n: Double, p: Int): Double = { val s = math pow (10, p); (math floor n * s) / s }

  def rps(duration: Double, invocations: Int): Double = {
    val rps = invocations / (duration / 1000)

    truncateAt(rps, 2)
  }
}
