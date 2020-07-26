scalaVersion := "2.13.1"

name := "hello-world"
organization := "ch.epfl.scala"
version := "1.0"

libraryDependencies += "org.typelevel" %% "cats-core" % "2.0.0"
libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.0" % Test

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "ch.epfl.scala",
      scalaVersion := "2.13.1"
    )),
    name := "hello-world"
  )

// ## カスタムタスクを作成してみる ##
// Keyの定義
val genCoverallsJson = taskKey[Unit]("Coverallsに送信するJSONを出力")

// genCoverallsJsonの実装
genCoverallsJson := {
  import org.scoverage.coveralls._

  val coverallsFile = crossTarget.value / "coveralls.json"
  val coberturaFile = crossTarget.value / "coverage-report" / "cobertura.xml"

  if (!coberturaFile.exists) {
    sys.error("Could not find the cobertura.xml file. Did you call coverageAggregate?")
  }

  val repoToken = sys.env.get("COVERALLS_REPO_TOKEN")

  if (repoToken.isEmpty) {
    sys.error("Could not find coveralls repo token.")
  }

  implicit val log = streams.value.log

  val repoRootDirectory = new File(".")

  val writer = new CoverallPayloadWriter(
    repoRootDirectory,
    coverallsFile,
    repoToken,
    None,
    Some("serviceName"), // coverallsServiceName.value,
    new GitClient(repoRootDirectory)(log)
  )

  writer.start()

  // // include all of the sources (from all modules)
  // val allSources = sourceDirectories.all(aggregateFilter).value.flatten.filter(_.isDirectory()).distinct
  // val reader = new CoberturaMultiSourceReader(coberturaFile, allSources, sourcesEnc)

  // log.info(s"Generating reports for ${reader.sourceFilenames.size} files")

  // val fileReports = reader.sourceFilenames.par.map(reader.reportForSource(_)).seq

  // log.info(s"Adding file reports to the coveralls file (${coverallsFile.value.getName})")

  // fileReports.foreach(writer.addSourceFile(_))

  writer.end()
}
