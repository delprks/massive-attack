package com.delprks.massiveattack.method.util

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.text.SimpleDateFormat
import java.util.Calendar

import com.delprks.massiveattack.method.result.{MethodDurationResult, MethodPerformanceResult}

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.mutable.ListBuffer

class ResultOps()(implicit ec: ExecutionContext) {

  def testResults(results: Future[ListBuffer[MethodDurationResult]], report: Boolean, reportName: Option[String]): Future[MethodPerformanceResult] = results map { response =>
    val responseDuration = response.map(_.duration.toInt)
    val average = avg(responseDuration)
    val invocationSeconds = response.map(_.endTime / 1000)
    val requestTimesPerSecond = invocationSeconds.groupBy(identity).map(_._2.size)

    val rpsAverage = avg(requestTimesPerSecond.toSeq)

    val testResult = MethodPerformanceResult(
      responseDuration.min,
      responseDuration.max,
      percentile(95)(responseDuration),
      percentile(99)(responseDuration),
      average,
      requestTimesPerSecond.min,
      requestTimesPerSecond.max,
      rpsAverage,
      response.size
    )

    if (report) {
      generateReport(responseDuration, testResult, reportName)
    }

    testResult
  }

  private def generateReport(responseDuration: ListBuffer[Int], testResult: MethodPerformanceResult, reportName: Option[String]): Unit = {
    val simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-S")
    val currentDateTime = simpleDateFormat.format(Calendar.getInstance().getTime)

    val generatedReportName = s"${reportName.getOrElse("performance_report")}-$currentDateTime.csv"

    print(Console.RED + s"Saving results to $generatedReportName... " + Console.RESET)

    Files.write(
      Paths.get(generatedReportName),
      (s"Response min (ms),Response max (ms),Response avg (ms),Response 95%tile (ms),Response 99%tile (ms)\n" +
        s"${testResult.responseTimeMin},${testResult.responseTimeMax},${testResult.responseTimeAvg},${testResult.responseTime95tile},${testResult.responseTime99tile}\n\n" +
        s"Invocations (total),Request min (rps),Request max (rps),Request avg (rps)\n" +
        s"${testResult.requests},${testResult.rpsMin},${testResult.rpsMax},${testResult.rpsAvg}\n\n" +
        responseDuration.mkString("Response times (ms)\n", "\n", "")
        ).getBytes(StandardCharsets.UTF_8)
    )

    println(Console.RED + "done" + Console.RESET)
  }

  private def avg(s: Seq[Int]): Int = s.foldLeft((0.0, 1))((acc, i) => (acc._1 + (i - acc._1) / acc._2, acc._2 + 1))._1.toInt

  private def percentile(p: Int)(seq: Seq[Int]) = {
    val sorted = seq.sorted
    val k = math.ceil((seq.length - 1) * (p / 100.0)).toInt

    sorted(k)
  }
}
