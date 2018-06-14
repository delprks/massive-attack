package com.delprks.massiveattack.method

case class MassiveAttackProperties(
  invocations: Int = 1000,
  threads: Int = 1,
  duration: Int = 30,
  warmUp: Boolean = true,
  warmUpInvocations: Int = 1000,
  verbose: Boolean = false
)
