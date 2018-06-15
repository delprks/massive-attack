package com.delprks.massiveattack.method

import java.util.concurrent.Executors

import com.delprks.massiveattack.MassiveAttack
import com.delprks.massiveattack.method.result.{MethodPerformanceResult, MethodDurationResult}
import com.delprks.massiveattack.method.util.{MethodOps, ResultOps}

import scala.collection.mutable.ListBuffer
import scala.collection.parallel.ForkJoinTaskSupport
import scala.collection.parallel.mutable.ParArray
import com.twitter.util.{Future => TwitterFuture}

import scala.concurrent.{ExecutionContext, Future => ScalaFuture}

class MethodPerformance(props: MethodPerformanceProps = MethodPerformanceProps()) extends MassiveAttack(props) {

  private implicit val context: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(props.threads))

  private val opsEC: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(20))

  private val methodOps = new MethodOps()(opsEC)
  private val resultOps = new ResultOps()

  def measure(longRunningMethod: () => TwitterFuture[_]): TwitterFuture[MethodPerformanceResult] = {
    if (props.warmUp) {
      println(s"Warming up the JVM...")

      methodOps.warmUpMethod(longRunningMethod, props.warmUpInvocations)
    }

    println(s"Invoking long running method ${props.invocations} times - or maximum ${props.duration} seconds")

    val testStartTime = System.currentTimeMillis()
    val testEndTime = testStartTime + props.duration * 1000

    val parallelInvocation: ParArray[Int] = (1 to props.invocations).toParArray
    parallelInvocation.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(props.threads))

    val results: ListBuffer[TwitterFuture[MethodDurationResult]] = methodOps.measure(parallelInvocation, () => longRunningMethod(), testEndTime)

    val testDuration: Double = (System.currentTimeMillis() - testStartTime).toDouble
    val testResultsF: TwitterFuture[MethodPerformanceResult] = resultOps.testResults(results)

    println(s"Test finished. Duration: ${testDuration / 1000}s")

    testResultsF map println

    testResultsF
  }

  def measure(longRunningMethod: () => ScalaFuture[_]): ScalaFuture[MethodPerformanceResult] = {
    if (props.warmUp) {
      println(s"Warming up the JVM...")

      methodOps.warmUpMethod(longRunningMethod, props.warmUpInvocations)
    }

    println(s"Invoking long running method ${props.invocations} times - or maximum ${props.duration} seconds")

    val testStartTime = System.currentTimeMillis()
    val testEndTime = testStartTime + props.duration * 1000

    val parallelInvocation: ParArray[Int] = (1 to props.invocations).toParArray
    parallelInvocation.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(props.threads))

    val results: ListBuffer[ScalaFuture[MethodDurationResult]] = methodOps.measure(parallelInvocation, () => longRunningMethod(), testEndTime, props.threads)


    println("result size is " + results.size)

    val testDuration: Long = System.currentTimeMillis() - testStartTime
    val testResultsF: ScalaFuture[MethodPerformanceResult] = resultOps.testResults(results)

    println(s"Test finished. Duration: ${testDuration / 1000}s")

    testResultsF map println

    testResultsF
  }

}
