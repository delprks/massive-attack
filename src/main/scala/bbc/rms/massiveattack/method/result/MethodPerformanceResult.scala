package bbc.rms.massiveattack.method.result

import bbc.rms.massiveattack.MassiveAttackResult

case class MethodPerformanceResult(
  responseTimeMin: Int,
  responseTimeMax: Int,
  responseTime95tile: Int,
  responseTime99tile: Int,
  responseTimeAvg: Int,
  rpsMin: Int,
  rpsMax: Int,
  rpsAvg: Int,
  requests: Int,
  spikes: Int,
  spikesPercentage: Double,
  spikesBoundary: Int
) extends MassiveAttackResult {

  def output(): Unit = {
    val align = "| %-25s | %-25s | %-25s | %n"

    printf(Console.YELLOW
         + "+---------------------------+---------------------------+---------------------------+%n")
    printf("| Response Times            | Requests                  | Spikes                    |%n")
    printf("+---------------------------+---------------------------+---------------------------+%n")
    printf(align, s"Minimum:       ${responseTimeMin}ms",    s"Invocations: $requests",    s"Count:      $spikes")
    printf(align, s"Maximum:       ${responseTimeMax}ms",    s"Minimum:     ${rpsMin}rps", s"Percentage: $spikesPercentage%")
    printf(align, s"Average:       ${responseTimeAvg}ms",    s"Maximum:     ${rpsMax}rps", s"Boundary:   ${spikesBoundary}ms")
    printf(align, s"95 percentile: ${responseTime95tile}ms", s"Average:     ${rpsAvg}rps", "")
    printf(align, s"99 percentile: ${responseTime99tile}ms", "", "")
    printf("+---------------------------+---------------------------+---------------------------+%n" + Console.RESET)
  }

}
