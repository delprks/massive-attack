package com.delprks.massiveattack.method

import com.delprks.massiveattack.method.util.ResultOps

case class TestResult(min: Long, max: Long, average: Double, rpsMin: Long, rpsMax: Long, rpsAvg: Double, requests: Int) extends ResultOps {
  override def toString= {
    s"""
      |"Min: ${min}ms"
      |"Max: ${max}ms"
      |"Average: ${average}ms"
      |"Invocations: $requests"
      |"Average requests: ${rpsAvg}rps"
      |"Min requests: ${rpsMin}rps"
      |"Max requests: ${rpsMax}rps"
    """.stripMargin
  }
}
