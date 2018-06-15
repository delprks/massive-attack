package com.delprks.massiveattack.method.util

import com.delprks.massiveattack.method.result.MethodDurationResult
import com.twitter.util.{Future => TwitterFuture}

import scala.collection.mutable.ListBuffer
import scala.collection.parallel.mutable.ParArray
import scala.concurrent.{ExecutionContext, Future => ScalaFuture}
import scala.reflect.ClassTag
import me.tongfei.progressbar.ProgressBar

class MethodOps()(implicit ec: ExecutionContext) {

  def measure(parallelInvocation: ParArray[Int], longRunningMethod: () => ScalaFuture[Any], testEndTime: Long, parallelism: Int): ListBuffer[ScalaFuture[MethodDurationResult]] = {
    var testResult = new ListBuffer[ScalaFuture[MethodDurationResult]]

    var counter = 0

    val progressBar = new ProgressBar(longRunningMethod.getClass.getSimpleName, parallelInvocation.length)
    progressBar.start()

    parallelInvocation.par.foreach { _ =>
      testResult += measureFutureDuration(longRunningMethod())
      counter += 1
//      progressBar.stepBy(parallelism)

//      println(testResult)

      if (System.currentTimeMillis() >= testEndTime) {
        println("count is " + counter + " and list size is " + testResult.size)
        return testResult
      }
    }

    println("count is " + counter + " and list size is " + testResult.size)
    testResult
  }

  def measure(parallelInvocation: ParArray[Int], longRunningMethod: () => TwitterFuture[Any], testEndTime: Long): ListBuffer[TwitterFuture[MethodDurationResult]] = {
    var testResult = new ListBuffer[TwitterFuture[MethodDurationResult]]

    parallelInvocation.par.foreach { _ =>
      testResult += measureFutureDuration(longRunningMethod())

      if (System.currentTimeMillis() >= testEndTime) {
        return testResult
      }
    }

    testResult
  }

  private def measureFutureDuration(method: => ScalaFuture[_]): ScalaFuture[MethodDurationResult] = {
    val currentTime = System.currentTimeMillis()

    method map { _ =>
      val timeAfterExecution = System.currentTimeMillis()

      MethodDurationResult(timeAfterExecution - currentTime, timeAfterExecution)
    }
  }

  private def measureFutureDuration(method: => TwitterFuture[_]): TwitterFuture[MethodDurationResult] = {
    val currentTime = System.currentTimeMillis()

    method map { _ =>
      val timeAfterExecution = System.currentTimeMillis()

      MethodDurationResult(timeAfterExecution - currentTime, timeAfterExecution)
    }
  }

  def warmUpMethod(longRunningMethod: () => ScalaFuture[_], warmUpInvocations: Int) =
    (1 to warmUpInvocations).foreach(_ => longRunningMethod())

  def warmUpMethod[X: ClassTag](longRunningMethod: () => TwitterFuture[_], warmUpInvocations: Int) =
    (1 to warmUpInvocations).foreach(_ => longRunningMethod())

}
