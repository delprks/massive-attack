# massive-attack

[![License](http://img.shields.io/:license-Apache%202-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.delprks/massive-attack_2.12/badge.svg)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.delprks%22%20AND%20a%3A%22massive-attack_2.12%22)

[![image](https://upload.wikimedia.org/wikipedia/en/a/a7/MassiveAttackBlueLines.jpg)](https://en.wikipedia.org/wiki/Blue_Lines)


Proprietor: RMS

Load generator test tool written in Scala used to benchmark any method that returns a Scala or Twitter `Future`.
Project forked from [massive-attack](https://github.com/delprks/massive-attack) project created and mantained by [Daniel Parks](https://github.com/delprks)

## Usage

1. Add it as a dependency to `build.sbt`:

`libraryDependencies += "bbc.rms" %% "massive-attack" % <release> % "test")`

2. Create your test in [ScalaTest](http://www.scalatest.org) or [Specs2](https://etorreborre.github.io/specs2) (this library might change to be a testing framework in future)

To test a long running method that returns a Future:

```scala
"long running method should have average response times of less than 40ms" in {
  val testProperties = MethodPerformanceProps(
    invocations = 10000,
    threads = 50,
    duration = 35,
    report = true,
    reportName = Some("scala_future_performance_test")
  )

  val methodPerformance = new MethodPerformance(testProperties)

  val testResultF: Future[MethodPerformanceResult] = methodPerformance.measure(() => method())
  val testResult = Await.result(testResultF, futureSupportTimeout)
  
  testResult.responseTimeAvg should be < 40
}
```

Which will result in:

![image](https://user-images.githubusercontent.com/8627976/41814261-6cbe9d1e-773f-11e8-94c4-c6e5e2825599.png)

And (if enabled) generate a report containing the test results:

![image](https://user-images.githubusercontent.com/8627976/41814268-92468eac-773f-11e8-8076-88b4ef9e17e1.png)

## Test properties

Following properties are available and configurable through `MassiveAttackProperties`:

### invocations

Specifies how many times the method should be invoked.
 
### threads

You can set how many threads you want to run the load test on - beware that Thrift clients can run only on single threads.

### duration

Specifies how long the method should be tested for in seconds - whichever comes first (duration or invocations) determines the length of the test.

### warmUp

This is by default set to `true` to avoid cold start times affecting the test results - set it to false if you want to test cold starts.

### warmUpInvocations

If `warmUp` is set to true, `warmUpInvocations` determines how many times the method should be invoked before the load test starts.

### spikeFactor

This is used to decide which response times should be considered as spikes, by multiplying the average response time and `spikeFactor`. It has the default
value of `3.0`.

### verbose

Set this to true if you want to see invocation times when the load test is in progress.

<h3>report</h3>

Set this to true if you want to save test results in a CSV file.

###  reportName

If `report` is set to true, results will be saved to this file. If no `reportName` is specified, one will be generated.

# License

original [massive-attack](https://github.com/delprks/massive-attack) is an open source software released under the [Apache 2 License](http://www.apache.org/licenses/LICENSE-2.0).
