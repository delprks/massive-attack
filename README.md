# massive-attack

[![License](http://img.shields.io/:license-Apache%202-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.delprks/massive-attack_2.12/badge.svg)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.delprks%22%20AND%20a%3A%22massive-attack_2.12%22)

[![image](https://upload.wikimedia.org/wikipedia/en/a/a7/MassiveAttackBlueLines.jpg)](https://en.wikipedia.org/wiki/Blue_Lines)

`massive-attack` is a simple and configurable load generator test tool written in Scala originally to test Apache Thrift endpoints, but it can be 
used to benchmark any method that returns a Scala or Twitter `Future`.

<h2>Why?</h2>

Because I needed to load test a Thrift endpoint in one of my APIs, and could not find an easy to use tool after looking around for days, at least
not one that did not require me to develop JMeter plugins, or one that has been updated in the past few years.
 
<h2>How?</h2>

So I decided to create one which is easy to use: 

1. It can easily be added as a dependency to your API or application
2. It is configurable as much or as little as you need it to be
3. It is extensible

<h2>Usage</h2>

1. Add it as a dependency to `build.sbt`:

`libraryDependencies ++= Seq("com.delprks" %% "massive-attack" % "1.0.0" % "test")`

2. Create your test in [ScalaTest](http://www.scalatest.org) or [Specs2](https://etorreborre.github.io/specs2) (this library might change to be a testing framework in future)

To test a long running method that returns a Future:

```scala
"long running method should have average response times of less than 40ms" in {
  val testProperties = MethodPerformanceProps(
    invocations = 10000,
    threads = 4,
    duration = 20
  )

  val methodPerformance = new MethodPerformance(testProperties)

  val testResultF: Future[MethodPerformanceResult] = methodPerformance.measure(() => method())
  val testResult = Await.result(testResultF, futureSupportTimeout)
  
  testResult.averageResponseTime should be < 40
}
```

Which will result in:

![image](https://user-images.githubusercontent.com/8627976/41440647-3a6c5ea6-7027-11e8-9248-9923447834fb.png)

<h2>Test properties</h2>

Following properties are available and configurable through `MassiveAttackProperties`:

<h3>invocations</h3>

Specifies how many times the method should be invoked.
 
<h3>threads</h3>

You can set how many threads you want to run the load test on - beware that Thrift clients can run only on single threads.

<h3>duration</h3>

Specifies how long the method should be tested for in seconds - whichever comes first (duration or invocations) determines the length of the test.

<h3>warmUp</h3>

This is by default set to `true` to avoid cold start times affecting the test results - set it to false if you want to test cold starts.

<h3>warmUpInvocations</h3>

If `warmUp` is set to true, `warmUpInvocations` determines how many times the method should be invoked before the load test starts.

<h3>verbose</h3>

Set this to true if you want to see invocation times when the load test is in progress.


# License

`massive-attack` is open source software released under the [Apache 2 License](http://www.apache.org/licenses/LICENSE-2.0).
