package com.delprks.massiveattack.method

import com.delprks.massiveattack.MassiveAttackProps

case class MethodPerformanceProps(
  invocations: Int = 1000,
  threads: Int = 1,
  duration: Int = 30,
  warmUp: Boolean = true,
  warmUpInvocations: Int = 500,
  spikeFactor: Double = 3.0,
  verbose: Boolean = false,
  report: Boolean = false,
  reportName: Option[String] = None
) extends MassiveAttackProps
