name := "MapCounter"

version := "1.0"

scalaVersion := "3.3.3"

lazy val pekkoVersion = "1.1.1"
lazy val osm4scalaVersion = "1.0.11"

// Run in a separate JVM, to make sure sbt waits until all threads have
// finished before returning.
// If you want to keep the application running while executing other
// sbt tasks, consider https://github.com/spray/sbt-revolver/
fork := true

libraryDependencies ++= Seq(
  "org.apache.pekko" %% "pekko-actor-typed" % pekkoVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.13",
  "org.apache.pekko" %% "pekko-actor-testkit-typed" % pekkoVersion % Test,
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  ("com.acervera.osm4scala" %% "osm4scala-core" % osm4scalaVersion).cross(CrossVersion.for3Use2_13)
)
