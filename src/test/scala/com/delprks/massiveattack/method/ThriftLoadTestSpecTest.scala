package com.delprks.massiveattack.method

import bbc.rms.metadata.thriftscala.{PlayableItem, ProgrammesService}
import bbc.rms.metadata.thriftscala.ProgrammesService.PlayableItems
import com.twitter.finagle.{Thrift, client}
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thriftmux.service.ThriftMuxResponseClassifier
import com.twitter.util
import org.specs2.mutable.Specification

import scala.concurrent.Future

class ThriftLoadTestSpecTest extends Specification {
  //  def longRunningMethod(): Future[Seq[String]] = {
  //    Thread.sleep(20)
  //
  //    Future.successful(Seq("Method finished running"))
  //  }
  //
  //  "PlayableItemMapper" should {
  //    "load test first expensive method" in {
  //      val loadTestSpec = new MethodLoadTest(threads = 600, invocations = 5000000, duration = 20)
  //
  //      loadTestSpec.test(() => longRunningMethod())
  //
  //      1 shouldEqual (1)
  //    }
  //  }

  "Playable endpoint should perform well" in {
    val host = "localhost:9911"

    val client: ProgrammesService.MethodPerEndpoint = Thrift.client.build[ProgrammesService.MethodPerEndpoint](host)

    val pids = Seq()

    val testProperties = MethodTestProperties(
      threads = 20,
      invocations = 10000000,
      duration = 310
    )

    val loadTestSpec = new MethodLoadTest(testProperties)

    loadTestSpec.test(() => client.playableItems(pids))

    1 shouldEqual 1
  }
}
