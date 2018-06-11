# massive-attack

[![License](http://img.shields.io/:license-Apache%202-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.delprks/massive-attack/badge.svg)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.delprks%22%20AND%20a%3A%22massive-attack%22)

`massive-attack` is a simple and configurable load test/generator tool written in Scala originally written to test Apache Thrift methods, but in theory can be 
used to benchmark any method that returns a `Future`.

<h2>Why?</h2>

Because I needed to load test a Thrift endpoint in one of my APIs, and could not find an easy to use tool after looking around for days, at least
not one that did not require me to develop JMeter plugins, or one that has been updated in the past few years and is easy to use.
 
<h2>How?</h2>

So I decided to create one which is easy to use: 

1. It can easily be added as a dependency to your API
2. It is configurable as much or as little as you need it to be
3. It is extensible

<h2>Usage</h2>

It is still in design phase; when it is ready for integration this section will be updated.

# License

`massive-attack` is open source software released under the [Apache 2 License](http://www.apache.org/licenses/LICENSE-2.0).
