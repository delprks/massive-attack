package com.delprks.massiveattack.method.result

import com.delprks.massiveattack.MassiveAttackResult

case class MethodPerformanceResult(
  responseTimeMin: Int,
  responseTimeMax: Int,
  responseTime95tile: Int,
  responseTime99tile: Int,
  responseTimeAvg: Int,
  rpsMin: Int,
  rpsMax: Int,
  rpsAvg: Int,
  requests: Int
) extends MassiveAttackResult {

  def output(): Unit = {
    val align = "| %-35s | %-35s | %n"

    printf(Console.YELLOW + "+-------------------------------------+-------------------------------------+%n")
    printf("| Response Times                      | Requests                            |%n")
    printf("+-------------------------------------+-------------------------------------+%n")
    printf(align, s"Min response:     ${responseTimeMin}ms",    s"Invocations:  $requests")
    printf(align, s"Max response:     ${responseTimeMax}ms",    s"Min requests: ${rpsMin}rps")
    printf(align, s"95%tile response: ${responseTime95tile}ms", s"Max requests: ${rpsMax}rps")
    printf(align, s"99%tile response: ${responseTime99tile}ms", s"Avg requests: ${rpsAvg}rps")
    printf(align, s"Avg response:     ${responseTimeAvg}ms", "")
    printf("+-------------------------------------+-------------------------------------+%n" + Console.RESET)
  }

}
