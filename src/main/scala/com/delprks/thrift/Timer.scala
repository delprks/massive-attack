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
}
