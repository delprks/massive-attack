package com.delprks.massiveattack.method

import java.util.concurrent.Executors

import akka.util.Timeout
import com.delprks.massiveattack.MassiveAttack
import com.delprks.massiveattack.method.result.{MethodDurationResult, MethodPerformanceResult}
import com.delprks.massiveattack.method.util.{MethodOps, ResultOps}

import scala.collection.mutable.ListBuffer
import scala.collection.parallel.ForkJoinTaskSupport
import scala.collection.parallel.mutable.ParArray
import com.twitter.util.{Future => TwitterFuture}

import scala.concurrent.duration._
import scala.concurrent.forkjoin.ForkJoinPool
import scala.concurrent.{ExecutionContext, Future => ScalaFuture}
import scala.reflect.ClassTag

class MethodPerformance(props: MethodPerformanceProps = MethodPerformanceProps()) extends MassiveAttack(props) {

  private val opsEC: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(props.threads * 2))
  private implicit val context: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(props.threads * 2))
  private implicit val timeout: Timeout = Timeout(5.seconds)

  private val methodOps = new MethodOps()(opsEC)
  private val resultOps = new ResultOps()(opsEC)

  override def measure(method: () => ScalaFuture[_]): ScalaFuture[MethodPerformanceResult] = {
    if (props.warmUp) {
      println(s"Warming up the JVM...")

      methodOps.warmUpMethod(method, props.warmUpInvocations)
    }

    println(s"Invoking method ${props.invocations} times - or maximum ${props.duration} seconds - on ${props.threads} threads")

    val testStartTime = System.currentTimeMillis()
    val testEndTime = testStartTime + props.duration * 1000
    val parallelInvocation: ParArray[Int] = (1 to props.invocations).toParArray
    val forkJoinPool = new ForkJoinPool(props.threads)

    parallelInvocation.tasksupport = new ForkJoinTaskSupport(forkJoinPool)

    val results: ScalaFuture[ListBuffer[MethodDurationResult]] = methodOps.measure(parallelInvocation, () => method(), testEndTime, props.threads)
    val testDuration: Long = System.currentTimeMillis() - testStartTime
    val testResultsF: ScalaFuture[MethodPerformanceResult] = resultOps.testResults(results)

    println(s"Test finished. Duration: ${testDuration / 1000}s")
    testResultsF map println

    testResultsF
  }

  override def measure[X: ClassTag](method: () => TwitterFuture[_]): ScalaFuture[MethodPerformanceResult] = {
    if (props.warmUp) {
      println(s"Warming up the JVM...")

      methodOps.warmUpMethod(method, props.warmUpInvocations)
    }

    println(s"Invoking method ${props.invocations} times - or maximum ${props.duration} seconds - on ${props.threads} threads")

    val testStartTime = System.currentTimeMillis()
    val testEndTime = testStartTime + props.duration * 1000
    val parallelInvocation: ParArray[Int] = (1 to props.invocations).toParArray
    val forkJoinPool = new scala.concurrent.forkjoin.ForkJoinPool(props.threads)

    parallelInvocation.tasksupport = new ForkJoinTaskSupport(forkJoinPool)

    val results: ScalaFuture[ListBuffer[MethodDurationResult]] = methodOps.measure(parallelInvocation, () => method(), testEndTime)
    val testDuration: Long = System.currentTimeMillis() - testStartTime
    val testResultsF: ScalaFuture[MethodPerformanceResult] = resultOps.testResults(results)

    println(s"Test finished. Duration: ${testDuration / 1000}s")
    testResultsF map println

    testResultsF
  }

}
