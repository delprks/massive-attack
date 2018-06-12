package com.delprks.massiveattack.method

import scala.concurrent.Future
import com.twitter.util.{Future => TwitterFuture, Return, Throw}

trait LoadGenerator {
  import scala.concurrent.ExecutionContext.Implicits.global

  def measure(method: => Future[_]): Future[MeasureResult] = {
    val currentTime = System.currentTimeMillis()

    method map { _ =>
      val timeAfterExecution = System.currentTimeMillis()

      MeasureResult(timeAfterExecution - currentTime, timeAfterExecution)
    }
  }

  def measure(method: => TwitterFuture[_]): TwitterFuture[MeasureResult] = {
    val currentTime = System.currentTimeMillis()

    method map { _ =>
      val timeAfterExecution = System.currentTimeMillis()

      MeasureResult(timeAfterExecution - currentTime, timeAfterExecution)
    }
  }

  def avg(s: Seq[Long]): Double = s.foldLeft((0.0, 1)) ((acc, i) => (acc._1 + (i - acc._1) / acc._2, acc._2 + 1))._1

  def truncateAt(n: Double, p: Int): Double = { val s = math pow (10, p); (math floor n * s) / s }

}
