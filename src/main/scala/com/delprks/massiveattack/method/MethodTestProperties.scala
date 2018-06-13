package com.delprks.massiveattack.method

case class MethodTestProperties(
  invocations: Int = 10000,
  threads: Int = 1,
  duration: Int = 30,
  warmUp: Boolean = true,
  warmUpInvocations: Int = 1000,
  verbose: Boolean = false
)
