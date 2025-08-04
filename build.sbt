import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}
import sbt.Keys._
import sbt._
import uk.gov.hmrc._
import DefaultBuildSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin

import scala.collection.Seq

lazy val playSettings: Seq[Setting[_]] = Seq.empty

val appName = "pla-dynamic-stub"
ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.16"

lazy val plugins: Seq[Plugins] = Seq.empty

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;uk.gov.hmrc.BuildInfo;app.*;prod.*;config.*;com.*",
    ScoverageKeys.coverageMinimumStmtTotal := 10,
    ScoverageKeys.coverageFailOnMinimum    := false,
    ScoverageKeys.coverageHighlighting     := true
  )
}

lazy val root = Project(appName, file("."))
  .enablePlugins(Seq(play.sbt.PlayScala, SbtDistributablesPlugin) ++ plugins: _*)
  .settings(playSettings ++ scoverageSettings: _*)
  .settings(scalaSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    libraryDependencies ++= AppDependencies(),
    dependencyOverrides += "commons-codec" % "commons-codec" % "1.12",
    parallelExecution in Test             := false,
    fork in Test                          := false,
    retrieveManaged                       := true,
    evictionWarningOptions in update      := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    scalacOptions += "-Wconf:cat=unused-imports&src=routes/.*:s"
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(integrationTestSettings())
