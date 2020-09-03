import ProjectInfo.ProjectName
import just.semver.SemVer

val GitHubUsername = "Kevin-Lee"
val RepoName = "fs2-kafka-example"

val ProjectNamePrefix = RepoName
val ProjectVersion = "0.1.0"
val ProjectScalaVersion = "2.13.3"

lazy val  hedgehogVersion: String = "0.5.0"

lazy val  hedgehogRepo: Resolver =
  "bintray-scala-hedgehog" at "https://dl.bintray.com/hedgehogqa/scala-hedgehog"

lazy val  hedgehogLibs: Seq[ModuleID] = Seq(
  "qa.hedgehog" %% "hedgehog-core" % hedgehogVersion % Test,
  "qa.hedgehog" %% "hedgehog-runner" % hedgehogVersion % Test,
  "qa.hedgehog" %% "hedgehog-sbt" % hedgehogVersion % Test,
)

lazy val cats: ModuleID = "org.typelevel" %% "cats-core" % "2.1.1"
lazy val catsEffect: ModuleID = "org.typelevel" %% "cats-effect" % "2.1.4"

lazy val pirateVersion = "44486bc961b52ba889f0b8f2b23f719d0ed8ba99"
lazy val pirateUri = uri(s"https://github.com/$GitHubUsername/pirate.git#$pirateVersion")

lazy val fs2Kafka = "com.github.fd4s" %% "fs2-kafka" % "1.0.0"
lazy val vulcan = "com.github.fd4s" %% "vulcan" % "1.1.0"

ThisBuild / scalaVersion := ProjectScalaVersion
ThisBuild / version := ProjectVersion
ThisBuild / organization     := "io.kevinlee"
ThisBuild / organizationName := "Kevin's Code"
ThisBuild / developers := List(
    Developer(GitHubUsername, "Kevin Lee", "kevin.code@kevinlee.io", url(s"https://github.com/$GitHubUsername"))
  )
ThisBuild / scmInfo :=
  Some(ScmInfo(
    url(s"https://github.com/$GitHubUsername/$RepoName")
    , s"https://github.com/$GitHubUsername/$RepoName.git"
  ))

def prefixedProjectName(name: String) = s"$RepoName${if (name.isEmpty) "" else s"-$name"}"

lazy val noPublish: SettingsDefinition = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  skip in sbt.Keys.`package` := true,
  skip in packagedArtifacts := true,
  skip in publish := true
)

def projectCommonSettings(id: String, projectName: ProjectName, file: File): Project =
  Project(id, file)
    .settings(
      name := prefixedProjectName(projectName.projectName),
      scalacOptions := (SemVer.parseUnsafe(scalaVersion.value) match {
        case SemVer(SemVer.Major(2), SemVer.Minor(13), SemVer.Patch(patch), _, _) =>
          val options = scalacOptions.value
          if (patch >= 3)
            options.filterNot(_ == "-Xlint:nullary-override")
          else
            options
        case _: SemVer =>
          scalacOptions.value
      })
      , testFrameworks ++= Seq(TestFramework("hedgehog.sbt.Framework"))
      , libraryDependencies ++= hedgehogLibs
    )


lazy val core = projectCommonSettings("core", ProjectName("core"), file("core"))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    libraryDependencies ++= Seq(cats, catsEffect, fs2Kafka, vulcan)
    /* Build Info { */
    , buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)
    , buildInfoObject := "Fs2KafkaExampleBuildInfo"
    , buildInfoPackage := "fs2kafka.example.info"
    , buildInfoOptions += BuildInfoOption.ToJson
    /* } Build Info */
    /* publish { */
    , licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
    /* } publish */
  )

lazy val cli = projectCommonSettings("cli", ProjectName("cli"), file("cli"))
  .enablePlugins(JavaAppPackaging)
  .settings(
      maintainer := "Kevin Lee <kevin.code@kevinlee.io>"
    , packageSummary := "FS2 Kafka Example"
    , packageDescription := "FS2 Kafka Example"
    , executableScriptName := ProjectNamePrefix
  )
  .dependsOn(core)

lazy val root = (project in file("."))
  .enablePlugins(DevOopsGitReleasePlugin)
  .settings(
      name := ProjectNamePrefix
    , addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)
    , addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
    /* GitHub Release { */
    , devOopsPackagedArtifacts := List(
        s"core/target/scala-*/${name.value}*.jar"
      , s"cli/target/universal/${name.value}*.zip"
      , s"cli/target/universal/${name.value}*.tgz"
      , s"cli/target/${name.value}*.deb"
    )
    /* } GitHub Release */
  )
  .settings(noPublish)
  .aggregate(core, cli)
