package bbc.rms.massiveattack.method.result

import akka.actor.Actor

import scala.collection.mutable.ListBuffer

case object GetStats

class MethodStatsRecorder extends Actor {
  var stats: ListBuffer[MethodDurationResult] = ListBuffer.empty[MethodDurationResult]

  override def receive: Receive = {
    case stat: MethodDurationResult => stats += stat

    case GetStats => sender() ! stats
  }

}
