package com.delprks.massiveattack.method.util

import com.delprks.massiveattack.method.MeasureResult
import com.twitter.util.{Future => TwitterFuture}

import scala.collection.mutable.ListBuffer
import scala.collection.parallel.mutable.ParArray
import scala.concurrent.{ExecutionContext, Future => ScalaFuture}
import scala.reflect.ClassTag

class MethodOps()(implicit ec: ExecutionContext) {

  def measure(parallelInvocation: => ParArray[Int], longRunningMethod: () => ScalaFuture[Any], testEndTime: Long): ListBuffer[ScalaFuture[MeasureResult]] = {
    var testResult = new ListBuffer[ScalaFuture[MeasureResult]]

    parallelInvocation.par.foreach { _ =>
      testResult += measureFutureDuration(longRunningMethod())

      if (System.currentTimeMillis() >= testEndTime) {
        return testResult
      }
    }

    testResult
  }

  def measure(parallelInvocation: ParArray[Int], longRunningMethod: () => TwitterFuture[Any], testEndTime: Long): ListBuffer[TwitterFuture[MeasureResult]] = {
    var testResult = new ListBuffer[TwitterFuture[MeasureResult]]

    parallelInvocation.par.foreach { _ =>
      testResult += measureFutureDuration(longRunningMethod())

      if (System.currentTimeMillis() >= testEndTime) {
        return testResult
      }
    }

    testResult
  }

  private def measureFutureDuration(method: => ScalaFuture[_]): ScalaFuture[MeasureResult] = {
    val currentTime = System.currentTimeMillis()

    method map { _ =>
      val timeAfterExecution = System.currentTimeMillis()

      MeasureResult(timeAfterExecution - currentTime, timeAfterExecution)
    }
  }

  private def measureFutureDuration(method: => TwitterFuture[_]): TwitterFuture[MeasureResult] = {
    val currentTime = System.currentTimeMillis()

    method map { _ =>
      val timeAfterExecution = System.currentTimeMillis()

      MeasureResult(timeAfterExecution - currentTime, timeAfterExecution)
    }
  }

  def warmUpMethod(longRunningMethod: () => ScalaFuture[_], warmUpInvocations: Int)=
    (1 to warmUpInvocations).foreach(_ => longRunningMethod())

  def warmUpMethod[X: ClassTag](longRunningMethod: () => TwitterFuture[_], warmUpInvocations: Int)=
    (1 to warmUpInvocations).foreach(_ => longRunningMethod())

}
