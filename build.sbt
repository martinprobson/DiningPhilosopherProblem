name := "DiningPhilosopherProblem"

version := "1.0"

scalaVersion := "2.13.6"

lazy val akkaVersion = "2.6.15"

libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
    "ch.qos.logback" % "logback-classic" % "1.2.5",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"

)
