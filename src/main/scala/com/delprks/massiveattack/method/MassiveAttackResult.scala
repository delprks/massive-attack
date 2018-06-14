package com.delprks.massiveattack.method

import com.delprks.massiveattack.method.util.ResultOps

case class MassiveAttackResult(
  minResponseTime: Long,
  maxResponseTime: Long,
  averageResponseTime: Int,
  rpsMin: Long,
  rpsMax: Long,
  rpsAvg: Int,
  requests: Int
) extends ResultOps {
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
