package com.delprks.massiveattack.method

import akka.actor.Actor
import com.delprks.massiveattack.method.result.MethodDurationResult

import scala.collection.mutable.ListBuffer

case object GetStats

class Recorder extends Actor {
  var stats: ListBuffer[MethodDurationResult] = ListBuffer.empty[MethodDurationResult]

  override def receive: Receive = {
    case stat: MethodDurationResult => stats += stat

    case GetStats => sender() ! stats
  }

}
