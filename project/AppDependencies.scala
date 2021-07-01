import sbt._

object AppDependencies {
import play.core.PlayVersion
  import play.sbt.PlayImport._

  private val bootstrapPlayVersion = "5.2.0"
  private val playReactiveMongoVersion = "8.0.0-play-28"
  private val pegdownVersion = "1.6.0"
  private val scalaTestPlusVersion = "5.1.0"
  private val domainVersion = "6.0.0-play-28"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % bootstrapPlayVersion,
    "uk.gov.hmrc" %% "simple-reactivemongo" % playReactiveMongoVersion,
    "uk.gov.hmrc" %% "stub-data-generator" % "0.5.3",
    "org.scalacheck" %% "scalacheck" % "1.14.3",
    "io.github.amrhassan" %% "scalacheck-cats" % "0.4.0"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "uk.gov.hmrc" %% "domain" % domainVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusVersion % scope,
        "org.scalatestplus"           %% "scalatestplus-mockito"      % "1.0.0-M2",
        "org.scalatestplus"           %% "scalatestplus-scalacheck"   % "3.1.0.0-RC2",
        "com.vladsch.flexmark"         %  "flexmark-all"              % "0.35.10"
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test()
}

