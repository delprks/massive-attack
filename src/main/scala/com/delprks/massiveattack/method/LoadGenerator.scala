package com.delprks.massiveattack.method

import com.delprks.massiveattack.method.util.ResultOps

import scala.concurrent.Future
import com.twitter.util.{Future => TwitterFuture}

trait LoadGenerator extends ResultOps {
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

}
