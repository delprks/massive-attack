package bbc.rms.massiveattack.method

import java.util.concurrent.Executors

import akka.util.Timeout
import bbc.rms.massiveattack.MassiveAttack
import bbc.rms.massiveattack.method.result.{MethodDurationResult, MethodPerformanceResult}
import bbc.rms.massiveattack.method.util.{MethodOps, ResultOps}
import scala.collection.parallel.CollectionConverters._


import scala.collection.mutable.ListBuffer
import com.twitter.util.{Future => TwitterFuture}

import scala.collection.parallel.ForkJoinTaskSupport
import scala.collection.parallel.mutable.ParArray
import scala.concurrent.duration._
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
      println(Console.BLUE + s"Warming up the JVM..." + Console.RESET)

      methodOps.warmUpMethod(method, props.warmUpInvocations)
    }

    println(Console.RED + s"Invoking method ${props.invocations} times - or maximum ${props.duration} seconds - on ${props.threads} threads" + Console.RESET)

    val testStartTime = System.currentTimeMillis()
    val testEndTime = testStartTime + props.duration * 1000
    val parallelInvocation: ParArray[Int] = (1 to props.invocations).toArray.par
    val forkJoinPool = new java.util.concurrent.ForkJoinPool(props.threads)

    parallelInvocation.tasksupport = new ForkJoinTaskSupport(forkJoinPool)

    val results: ScalaFuture[ListBuffer[MethodDurationResult]] = methodOps.measure(parallelInvocation, () => method(), testEndTime)
    val testDuration: Long = System.currentTimeMillis() - testStartTime
    val testResultsF: ScalaFuture[MethodPerformanceResult] = resultOps.testResults(results, props)

    println(Console.GREEN + s"Performance test finished. Duration: ${testDuration / 1000}s" + Console.RESET)

    testResultsF map (_.output())

    testResultsF
  }

  override def measure[X: ClassTag](method: () => TwitterFuture[_]): ScalaFuture[MethodPerformanceResult] = {
    if (props.warmUp) {
      println(Console.BLUE + s"Warming up the JVM..." + Console.RESET)

      methodOps.warmUpMethod(method, props.warmUpInvocations)
    }

    println(Console.RED + s"Invoking method ${props.invocations} times - or maximum ${props.duration} seconds - on ${props.threads} threads" + Console.RESET)


    val testStartTime = System.currentTimeMillis()
    val testEndTime = testStartTime + props.duration * 1000
    val parallelInvocation: ParArray[Int] = (1 to props.invocations).toArray.par
    val forkJoinPool = new java.util.concurrent.ForkJoinPool(props.threads)

    parallelInvocation.tasksupport = new ForkJoinTaskSupport(forkJoinPool)

    val results: ScalaFuture[ListBuffer[MethodDurationResult]] = methodOps.measure(parallelInvocation, () => method(), testEndTime)
    val testDuration: Long = System.currentTimeMillis() - testStartTime
    val testResultsF: ScalaFuture[MethodPerformanceResult] = resultOps.testResults(results, props)

    println(Console.GREEN + s"Performance test finished. Duration: ${testDuration / 1000}s" + Console.RESET)

    testResultsF map (_.output())

    testResultsF
  }

}
