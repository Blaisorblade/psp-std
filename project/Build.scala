package psp
package build

import scala.Predef.{ conforms => _ }
import sbt._, Keys._, psp.libsbt._, Deps._
import psp.std._
import scoverage.ScoverageKeys._

object Build extends sbt.Build {
  def isAmmoniteDebug     = sys.env contains "AMMONITE_DEBUG"
  def ammoniteVersion     = if (isAmmoniteDebug) "0.5.1-SNAPSHOT" else "0.5.0"
  def ammoniteDep         = "com.lihaoyi" % "ammonite-repl" % ammoniteVersion cross CrossVersion.full
  def consoleDependencies = List(jsr305, ammoniteDep)
  def optimizeArgs        = wordSeq("-optimise -Yinline-warnings")
  def stdArgs             = wordSeq("-language:_ -Yno-predef -Yno-adapted-args -Yno-imports -unchecked") // -Ymacro-debug-verbose
  def testDependencies    = Def setting Seq(Deps.scalaReflect.value, scalacheck.copy(configurations = None))

  lazy val api = project setup "psp's non-standard api" also spire
  lazy val std = project setup "psp's non-standard standard library" dependsOn api

  /***
   *** Everything below this line is to navigate the maze that is sbt.
   ***/

  def subprojects   = List[sbt.Project](api, std)
  def projectRefs   = convertSeq(subprojects): List[ProjectReference]
  def classpathDeps = convertSeq(subprojects): List[ClasspathDep[ProjectReference]]

  val ammoniteTask = Def task {
    val forker    = new Fork("java", Some("psp.ReplMain"))
    val files     = (fullClasspath in Compile in LocalProject("consoleOnly")).value.files filterNot (_.toString contains "scoverage")
    val classpath = files mkString ":"
    val jvmArgs   = sciSeq(s"-Xbootclasspath/a:$classpath") // boot classpath way faster
    val forkOpts  = ForkOptions(outputStrategy = Some(StdoutOutput), connectInput = true, runJVMOptions = jvmArgs)

    if (isAmmoniteDebug)
      println(files mkString ("", "\n", "\n"))

    forker(forkOpts, "-usejavacp" +: stdArgs)
  }

  implicit class ProjectOps(val p: Project) {
    import p.id
    def aggregatesAll = p aggregate (projectRefs: _*)
    def dependsOnAll  = p dependsOn (classpathDeps: _*)
    def allSources    = Def task (sources in Test in p).value ++ (sources in Compile in p).value
    def usesCompiler  = p settings (libraryDependencies += Deps.scalaCompiler.value)
    def usesReflect   = p settings (libraryDependencies += Deps.scalaReflect.value)
    def crossSettings = if (id == "root") Nil else Seq(target <<= javaCrossTarget(id))

    def setup(): Project             = p.alsoToolsJar also commonSettings(p) also (name := s"psp-$id")
    def setup(text: String): Project = setup() also (description := text)
    def hidden(): Project            = p in file(s"./project/$id")
    def helper(): Project            = p.hidden.noArtifacts setup s"helper project $id" dependsOn (classpathDeps: _*)
  }

  // updateOptions ~=  (_ withCachedResolution true)
  private def commonSettings(p: Project) = standardSettings ++ Seq(
       externalResolvers :=  Seq(Resolver.defaultLocal, "google" at "http://maven-central.storage.googleapis.com", Resolver.jcenterRepo),
                 version :=  sbtBuildProps.buildVersion,
            scalaVersion :=  scalaVersionLatest,
      crossScalaVersions :=  Seq(scalaVersion.value),
                licenses :=  pspLicenses,
            organization :=  pspOrg,
           scalacOptions ++= scalacOptionsFor(scalaBinaryVersion.value) ++ stdArgs,
        triggeredMessage :=  Watched.clearWhenTriggered,
              incOptions ~=  (_ withNameHashing false)
  ) ++ p.crossSettings

  lazy val root = project.root.setup.aggregatesAll.dependsOnAll settings (
    console in Compile <<=  console in Compile in consoleOnly,
       console in Test <<=  console in Test in consoleOnly,
          watchSources <++= testOnly.allSources,
          watchSources <++= consoleOnly.allSources,
                  test <<=  test in testOnly
  ) also addCommandAlias("cover", "; clean ; coverage ; test ; coverageReport")

  lazy val consoleOnly = ( project.helper.dependsOnAll
    dependsOn (testOnly % "test->test")
         deps (consoleDependencies: _*)
     settings (console in Compile := ammoniteTask.value)
  )
  lazy val testOnly = project.helper.dependsOnAll.aggregatesAll settings (
     testOptions in Test  +=  Tests.Argument(TestFrameworks.ScalaCheck, "-verbosity", "1"),
             logBuffered  :=  false,
     libraryDependencies  +=  scalacheck,
     libraryDependencies <++= testDependencies,
                    test  :=  (run in Test toTask "").value
  )
}
