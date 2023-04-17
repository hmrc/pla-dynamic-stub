import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}
import sbt.Keys._
import sbt._
import uk.gov.hmrc._
import DefaultBuildSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin

lazy val plugins : Seq[Plugins] = Seq(play.sbt.PlayScala)
lazy val playSettings : Seq[Setting[_]] = Seq.empty

val appName = "pla-dynamic-stub"
val silencerVersion = "1.7.1"

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;uk.gov.hmrc.BuildInfo;app.*;prod.*;config.*;com.*",
    ScoverageKeys.coverageMinimumStmtTotal := 10,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )
}

lazy val root = Project(appName, file("."))
  .enablePlugins(Seq(play.sbt.PlayScala, SbtDistributablesPlugin) ++ plugins : _*)
  .settings(playSettings ++ scoverageSettings : _*)
  .settings(scalaSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    scalaVersion := "2.12.12",
    libraryDependencies ++= AppDependencies(),
    dependencyOverrides += "commons-codec" % "commons-codec" % "1.12",
    parallelExecution in Test := false,
    fork in Test := false,
    retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    scalacOptions += "-P:silencer:pathFilters=routes",
    scalacOptions += "-P:silencer:lineContentFilters=^\\w",
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    )
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(integrationTestSettings())
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(majorVersion := 0)
