import sbt.Keys.pomExtra
import sbt.{Classpaths, Test}

ThisBuild / organization := "bbc.rms"
ThisBuild / scalaVersion := "2.12.2"
//  "2.13.2" TODO federico cocco 16/06/2020: Cannot crossbuild. Update to 2.13 once all projects using this library are 2.13 too
ThisBuild / name := "massive-attack"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

resolvers ++= Seq("BBC Artifactory" at "https://artifactory.dev.bbc.co.uk/artifactory/repo/") :+ Classpaths.typesafeReleases

lazy val mavenStyleSettings = Seq(
  publishMavenStyle := true,
  pomIncludeRepository := {
    _ => false
  },
  pomExtra := {
    <url>https://github.com/delprks/massive-attack</url>
      <licenses>
        <license>
          <name>Apache 2</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:delprks/massive-attack.git</url>
        <connection>scm:git@github.com:delprks/massive-attack.git</connection>
      </scm>
      <developers>
        <developer>
          <id>delprks</id>
          <name>Daniel Parks</name>
          <url>http://github.com/delprks</url>
        </developer>
      </developers>
  }
)

SbtPgp.autoImport.useGpgAgent := true

lazy val publishSettings = Seq(
  version := scala.util.Properties.envOrElse("BUILD_VERSION", "0.1-SNAPSHOT"),
  publishArtifact in (Test, packageBin) := true,
  publishMavenStyle := true,
  publishTo := Some("BBC Repository" at "https://artifactory.dev.bbc.co.uk/artifactory/int-bbc-releases"),
  credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
)

lazy val testSettings = Seq(
  Test / publishArtifact := false,
  Test / parallelExecution := false
)


libraryDependencies ++= Seq(
  "com.twitter" %% "util-core" % "20.5.0",
  "com.typesafe.akka" %% "akka-actor" % "2.6.5",
  "com.typesafe.akka" %% "akka-testkit" % "2.6.5" % Test,
  "org.scalatest" %% "scalatest" % "3.1.2" % Test
) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
  case Some((2, major)) if major <= 12 =>
    Seq()
  case _ =>
    Seq("org.scala-lang.modules" %% "scala-parallel-collections" % "0.2.0")
})


run /connectInput := true

run / fork := true

lazy val `massive-attack` = (project in file("."))
  .settings(publishSettings)
  .settings(testSettings)
  .settings(mavenStyleSettings)
