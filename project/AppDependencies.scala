import sbt._

object AppDependencies {
  import play.sbt.PlayImport._

  private val bootstrapPlayVersion = "7.15.0"
  private val hmrcMongoVersion = "1.1.0"
  private val domainVersion = "8.2.0-play-28"

  val compile = Seq(
    ws,
    "uk.gov.hmrc"         %% "bootstrap-backend-play-28" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"   %% "hmrc-mongo-play-28"        % hmrcMongoVersion,
    "uk.gov.hmrc"         %% "stub-data-generator"       % "1.0.0"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % bootstrapPlayVersion % scope,
        "uk.gov.hmrc"             %% "domain"                     % domainVersion       % scope,
        "org.scalatestplus"       %% "scalatestplus-mockito"      % "1.0.0-M2"          % scope,
        "org.scalatestplus"       %% "scalatestplus-scalacheck"   % "3.1.0.0-RC2"       % scope,
        "org.scalacheck"          %% "scalacheck"                 % "1.17.0"            % scope,
        "io.github.amrhassan"     %% "scalacheck-cats"            % "0.4.0"             % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test()
}

