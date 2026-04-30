import sbt.*

object AppDependencies {
  import play.sbt.PlayImport.*

  private val bootstrapPlayVersion = "10.7.0"
  private val hmrcMongoVersion     = "2.12.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "stub-data-generator"       % "1.4.0"
  )

  trait TestDependencies {
    lazy val scope: String       = "test"
    val test: Seq[ModuleID]
  }

  object Test {

    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test: Seq[ModuleID] = Seq(
        "uk.gov.hmrc"       %% "bootstrap-test-play-30"   % bootstrapPlayVersion % scope,
        "uk.gov.hmrc"       %% "domain-play-30"           % "11.0.0"             % scope,
        "org.scalatestplus" %% "mockito-5-23"    % "3.2.20.0"           % scope,
        "org.scalatestplus" %% "scalacheck-1-18" % "3.2.19.0"        % scope,
        "org.scalacheck"    %% "scalacheck"               % "1.19.0"             % scope,
        "io.chrisdavenport" %% "cats-scalacheck"          % "0.3.2"              % scope
      )
    }.test

  }

  def apply(): Seq[ModuleID] = compile ++ Test()
}
