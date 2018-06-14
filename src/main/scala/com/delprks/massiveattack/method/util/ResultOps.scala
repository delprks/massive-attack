package com.delprks.massiveattack.method.util

import com.delprks.massiveattack.method.result.{MethodPerformanceResult, MethodDurationResult}
import com.twitter.util.{Future => TwitterFuture}

import scala.concurrent.{ExecutionContext, Future => ScalaFuture}
import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

class ResultOps()(implicit ec: ExecutionContext) {

  def testResults(results: ListBuffer[ScalaFuture[MethodDurationResult]]): ScalaFuture[MethodPerformanceResult] = ScalaFuture.sequence(results).map { response =>
    val responseDuration = response.map(_.duration.toInt)
    val average = avg(responseDuration)
    val invocationSeconds = response.map(_.endTime / 1000)
    val requestTimesPerSecond = invocationSeconds.groupBy(identity).map(_._2.size)

    val rpsAverage = avg(requestTimesPerSecond.toSeq)

    MethodPerformanceResult(responseDuration.min, responseDuration.max, average, requestTimesPerSecond.min, requestTimesPerSecond.max, rpsAverage, response.size)
  }

  def testResults[X: ClassTag](results: ListBuffer[TwitterFuture[MethodDurationResult]]): TwitterFuture[MethodPerformanceResult] = TwitterFuture.collect(results).map { response =>
    val responseDuration = response.map(_.duration.toInt)
    val average = avg(responseDuration)
    val invocationSeconds = response.map(_.endTime / 1000)
    val requestTimesPerSecond = invocationSeconds.groupBy(identity).map(_._2.size)

    val rpsAverage = avg(requestTimesPerSecond.toSeq)

    MethodPerformanceResult(responseDuration.min, responseDuration.max, average, requestTimesPerSecond.min, requestTimesPerSecond.max, rpsAverage, response.size)
  }

  private def avg(s: Seq[Int]): Int = s.foldLeft((0.0, 1))((acc, i) => (acc._1 + (i - acc._1) / acc._2, acc._2 + 1))._1.toInt

}
