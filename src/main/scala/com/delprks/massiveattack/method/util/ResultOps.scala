package com.delprks.massiveattack.method.util

import com.delprks.massiveattack.method.result.{MethodDurationResult, MethodPerformanceResult}
import com.twitter.util.{Future => TwitterFuture}

import scala.concurrent.{ExecutionContext, Future => ScalaFuture}
import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag
import scala.util.Try

class ResultOps()(implicit ec: ExecutionContext) {

  def testResults(results: ListBuffer[ScalaFuture[MethodDurationResult]]): ScalaFuture[MethodPerformanceResult] = {
    var counter = 0
    import scala.util.{Failure, Success}

    println("response size right before sequence is " + results.length)

    def lift[T](futures: Seq[ScalaFuture[T]]) =
      futures.map(_.map {
        Success(_)
      }.recover { case t => Failure(t) })

    def waitAll[T](futures: Seq[ScalaFuture[T]]) =
      ScalaFuture.sequence(lift(futures))

    waitAll(results).map { tryResponse =>
      val response: Seq[MethodDurationResult] = tryResponse.flatMap(_.toOption)

      println("response size in future is " + response.size)

      val responseDuration = response.map(_.duration.toInt)
      val average = avg(responseDuration)
      val invocationSeconds = response.map(_.endTime / 1000)
      val requestTimesPerSecond = invocationSeconds.groupBy(identity).map(_._2.size)

      val rpsAverage = avg(requestTimesPerSecond.toSeq)

      MethodPerformanceResult(responseDuration.min, responseDuration.max, average, requestTimesPerSecond.min, requestTimesPerSecond.max, rpsAverage, results.size)
    }

  }

  //use liftToTry (https://stackoverflow.com/questions/29344430/scala-waiting-for-sequence-of-futures)
  def testResults[X: ClassTag](results: ListBuffer[TwitterFuture[MethodDurationResult]]): TwitterFuture[MethodPerformanceResult] = TwitterFuture.collect(results).map { response =>
    val responseDuration = response.map(_.duration.toInt)
    val average = avg(responseDuration)
    val invocationSeconds = response.map(_.endTime / 1000)
    val requestTimesPerSecond = invocationSeconds.groupBy(identity).map(_._2.size)

    val rpsAverage = avg(requestTimesPerSecond.toSeq)

    MethodPerformanceResult(responseDuration.min, responseDuration.max, average, requestTimesPerSecond.min, requestTimesPerSecond.max, rpsAverage, results.size)
  }

  private def avg(s: Seq[Int]): Int = s.foldLeft((0.0, 1))((acc, i) => (acc._1 + (i - acc._1) / acc._2, acc._2 + 1))._1.toInt

}
