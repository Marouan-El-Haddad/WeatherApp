ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "WeatherApp"
  )

// Add the necessary dependencies
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.5.0",
  "com.typesafe.akka" %% "akka-stream" % "2.8.0",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.5.0",
  "com.typesafe" % "config" % "1.4.2",
  "de.heikoseeberger" %% "akka-http-play-json" % "1.39.2",
  "com.softwaremill.sttp.client3" %% "async-http-client-backend-future" % "3.8.13",
  "io.circe" %% "circe-core" % "0.14.5",
  "io.circe" %% "circe-generic" % "0.14.5",
  "io.circe" %% "circe-parser" % "0.14.5",
  "io.circe" %% "circe-optics" % "0.14.1",
  "com.softwaremill.sttp.client3" %% "circe" % "3.8.13",
  "ch.qos.logback" % "logback-classic" % "1.4.6",
)


// Enable the use of the application.conf file
fork in run := true

