package com.delprks.massiveattack.method.result

import com.delprks.massiveattack.MassiveAttackResult

case class MethodPerformanceResult(
  minResponseTime: Int,
  maxResponseTime: Int,
  averageResponseTime: Int,
  rpsMin: Int,
  rpsMax: Int,
  rpsAvg: Int,
  requests: Int
) extends MassiveAttackResult {
  override def toString: String = {
    s"""
       |Min: ${minResponseTime}ms
       |Max: ${maxResponseTime}ms
       |Average: ${averageResponseTime}ms
       |Invocations: $requests
       |Average requests: ${rpsAvg}rps
       |Min requests: ${rpsMin}rps
       |Max requests: ${rpsMax}rps
    """.stripMargin
  }
}
