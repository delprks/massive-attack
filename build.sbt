name := "massive-attack"

crossScalaVersions := Seq("2.12.6", "2.11.12")

organization := "com.delprks"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

publishTo := {
  val isSnapshotValue = isSnapshot.value
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshotValue) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

parallelExecution in Test := false

releaseCrossBuild := true

sbtrelease.ReleasePlugin.autoImport.releasePublishArtifactsAction := PgpKeys.publishSigned.value

sbtrelease.ReleasePlugin.autoImport.releaseCrossBuild := false

SbtPgp.autoImport.useGpg := true

SbtPgp.autoImport.useGpgAgent := true

libraryDependencies ++= Seq(
  "com.twitter" %% "util-core" % "18.5.0",
  "me.tongfei" % "progressbar" % "0.4.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "org.specs2" %% "specs2-core" % "3.9.2" % "test"
)

pomIncludeRepository := {
  _ => false
}

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

connectInput in run := true

fork in run := true

lazy val root = project in file(".")

