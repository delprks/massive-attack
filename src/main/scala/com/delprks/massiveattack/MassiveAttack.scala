package com.delprks.massiveattack

import com.twitter.util.{Future => TwitterFuture}

import scala.concurrent.{Future => ScalaFuture}

abstract class MassiveAttack(props: MassiveAttackProps) {

  def measure(longRunningMethod: () => ScalaFuture[_]): ScalaFuture[MassiveAttackResult]

  def measure(longRunningMethod: () => TwitterFuture[_]): TwitterFuture[MassiveAttackResult]

}
