package com.delprks.massiveattack

import com.twitter.util.{Future => TwitterFuture}

import scala.concurrent.{Future => ScalaFuture}
import scala.reflect.ClassTag

abstract class MassiveAttack(props: MassiveAttackProps) {

  def measure(method: () => ScalaFuture[_]): ScalaFuture[MassiveAttackResult]

  def measure[X: ClassTag](method: () => TwitterFuture[_]): ScalaFuture[MassiveAttackResult]

}
