package com.delprks.massiveattack.method.util

import com.delprks.massiveattack.method.{MassiveAttackResult, MeasureResult}
import com.twitter.util.{Future => TwitterFuture}

import scala.concurrent.{Future => ScalaFuture}
import scala.collection.mutable.ListBuffer

class ResultOps {

  import scala.concurrent.ExecutionContext.Implicits.global

  def scalaTestResults(results: ListBuffer[ScalaFuture[MeasureResult]]): ScalaFuture[MassiveAttackResult] = ScalaFuture.sequence(results).map { response =>
    val responseDuration = response.map(_.duration.toInt)
    val average = avg(responseDuration)
    val invocationSeconds = response.map(_.endTime / 1000)
    val requestTimesPerSecond = invocationSeconds.groupBy(identity).map(_._2.size)

    val rpsAverage = avg(requestTimesPerSecond.toSeq)

    MassiveAttackResult(responseDuration.min, responseDuration.max, average, requestTimesPerSecond.min, requestTimesPerSecond.max, rpsAverage, response.size)
  }

  def twitterTestResults(results: ListBuffer[TwitterFuture[MeasureResult]]): TwitterFuture[MassiveAttackResult] = TwitterFuture.collect(results).map { response =>
    val responseDuration = response.map(_.duration.toInt)
    val average = avg(responseDuration)
    val invocationSeconds = response.map(_.endTime / 1000)
    val requestTimesPerSecond = invocationSeconds.groupBy(identity).map(_._2.size)

    val rpsAverage = avg(requestTimesPerSecond.toSeq)

    MassiveAttackResult(responseDuration.min, responseDuration.max, average, requestTimesPerSecond.min, requestTimesPerSecond.max, rpsAverage, response.size)
  }

  private def avg(s: Seq[Int]): Int = s.foldLeft((0.0, 1))((acc, i) => (acc._1 + (i - acc._1) / acc._2, acc._2 + 1))._1.toInt

}
