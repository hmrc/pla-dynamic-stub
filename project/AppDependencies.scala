import sbt._

object AppDependencies {
import play.core.PlayVersion
  import play.sbt.PlayImport._

  private val bootstrapPlayVersion = "5.24.0"
  private val playReactiveMongoVersion = "8.1.0-play-28"
  private val pegdownVersion = "1.6.0"
  private val domainVersion = "8.1.0-play-28"

  val compile = Seq(
    ws,
    "uk.gov.hmrc"         %% "bootstrap-backend-play-28" % bootstrapPlayVersion,
    "uk.gov.hmrc"         %% "simple-reactivemongo"      % playReactiveMongoVersion,
    "uk.gov.hmrc"         %% "stub-data-generator"       % "0.5.3",
    "org.scalacheck"      %% "scalacheck"                % "1.16.0",
    "io.github.amrhassan" %% "scalacheck-cats"           % "0.4.0"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % "5.25.0"            % scope,
        "org.pegdown"              % "pegdown"                    % pegdownVersion      % scope,
        "com.typesafe.play"       %% "play-test"                  % PlayVersion.current % scope,
        "uk.gov.hmrc"             %% "domain"                     % domainVersion       % scope,
        "org.scalatestplus"       %% "scalatestplus-mockito"      % "1.0.0-M2",
        "org.scalatestplus"       %% "scalatestplus-scalacheck"   % "3.1.0.0-RC2"
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test()
}

