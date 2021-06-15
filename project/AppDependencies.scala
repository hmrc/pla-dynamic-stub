import sbt._

object AppDependencies {
import play.core.PlayVersion
  import play.sbt.PlayImport._

  private val bootstrapPlayVersion = "5.3.0"
  private val playReactiveMongoVersion = "8.0.0-play-27"
  private val pegdownVersion = "1.6.0"
  private val scalaTestVersion = "3.0.8"
  private val scalaTestPlusVersion = "3.1.3"
  private val domainVersion = "5.11.0-play-27"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-27" % bootstrapPlayVersion,
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
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.mockito" % "mockito-core" % "2.3.4" % scope,
        "uk.gov.hmrc" %% "domain" % domainVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusVersion % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test()
}

