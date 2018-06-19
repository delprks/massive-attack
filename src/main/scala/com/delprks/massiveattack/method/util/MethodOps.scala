package com.delprks.massiveattack.method.util

import akka.actor.{ActorSystem, Props}
import com.delprks.massiveattack.method.result.{GetStats, MethodDurationResult, MethodStatsRecorder}
import com.twitter.util.{Future => TwitterFuture}

import scala.collection.mutable.ListBuffer
import scala.collection.parallel.mutable.ParArray
import scala.concurrent.{Future => ScalaFuture}
import scala.reflect.ClassTag
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

class MethodOps()(implicit ec: ExecutionContext) {

  private val system = ActorSystem("massive-attack")
  private val recorder = system.actorOf(Props[MethodStatsRecorder], "test-result-recorder")
  implicit val timeout: Timeout = Timeout(5.seconds)

  def measure(parallelInvocation: ParArray[Int], longRunningMethod: () => ScalaFuture[Any], testEndTime: Long, parallelism: Int): ScalaFuture[ListBuffer[MethodDurationResult]] = {
    parallelInvocation.par.foreach { _ =>
      measureFutureDuration(longRunningMethod())

      if (System.currentTimeMillis() >= testEndTime) {
        return (recorder ? GetStats).asInstanceOf[Future[ListBuffer[MethodDurationResult]]]
      }
    }

    (recorder ? GetStats).asInstanceOf[Future[ListBuffer[MethodDurationResult]]]
  }

  def measure(parallelInvocation: ParArray[Int], longRunningMethod: () => TwitterFuture[Any], testEndTime: Long): ScalaFuture[ListBuffer[MethodDurationResult]] = {
    parallelInvocation.par.foreach { _ =>
      measureFutureDuration(longRunningMethod())

      if (System.currentTimeMillis() >= testEndTime) {
        return (recorder ? GetStats).asInstanceOf[Future[ListBuffer[MethodDurationResult]]]
      }
    }

    (recorder ? GetStats).asInstanceOf[Future[ListBuffer[MethodDurationResult]]]
  }

  private def measureFutureDuration(method: => ScalaFuture[_])= {
    val currentTime = System.currentTimeMillis()

    method map { _ =>
      val timeAfterExecution = System.currentTimeMillis()

      recorder ! MethodDurationResult(timeAfterExecution - currentTime, timeAfterExecution)
    }
  }

  private def measureFutureDuration(method: => TwitterFuture[_])= {
    val currentTime = System.currentTimeMillis()

    method map { _ =>
      val timeAfterExecution = System.currentTimeMillis()

      recorder ! MethodDurationResult(timeAfterExecution - currentTime, timeAfterExecution)
    }
  }

  def warmUpMethod(longRunningMethod: () => ScalaFuture[_], warmUpInvocations: Int) =
    (1 to warmUpInvocations).foreach(_ => longRunningMethod())

  def warmUpMethod[X: ClassTag](longRunningMethod: () => TwitterFuture[_], warmUpInvocations: Int) =
    (1 to warmUpInvocations).foreach(_ => longRunningMethod())

}
