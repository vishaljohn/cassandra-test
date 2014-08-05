import sbt._
import sbt.Classpaths.publishTask
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object Assembly extends Build {

  lazy val root = Project(
    "cassandra-test",
    file("."),
    settings = Defaults.defaultSettings ++ Seq(
      version := "1.0",
      organization := "Vishal",
      sourcesInBase := true,
      scalaVersion := "2.10.3",
      compileOrder := CompileOrder.Mixed,
      resolvers ++= Seq(
        "Maven2" at "http://repo1.maven.org/maven2",
        "Eclipse Repository" at "https://repo.eclipse.org/content/repositories/paho-releases/",
        "Apache repo" at "https://repository.apache.org/content/repositories/releases"
      ),
      libraryDependencies ++= Seq() ++ Seq(
        "com.datastax.cassandra" % "cassandra-driver-core" % "1.0.2"
          exclude ("org.apache.cassandra.deps", "avro")
          exclude ("org.slf4j", "slf4j-log4j12")
      )
    ) ++ assemblySettings ++ Seq(
      jarName in assembly := "cassandra-test.jar",
      mainClass in assembly := Some("vj.test.CassandraTester")
    )
  )

}
