import Dependencies._

lazy val root = (project in file(".")).settings(
  inThisBuild(List(organization := "com.codacy", scalaVersion := "2.12.6", version := "0.1.0-SNAPSHOT")),
  name := "codacy-solhint",
  libraryDependencies ++= Seq(codacySeed, scalaTest % Test)
)
