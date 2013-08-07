import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "sparql"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    javacOptions += "-Xlint:deprecation",
    // Add your own project settings here
    resolvers += Resolver.url("play-plugin-releases", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns),
    resolvers += Resolver.url("play-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns),
    resolvers += "Mnemotix Nexus (snapshots)" at "http://dev.mnemotix.com:8080/nexus/content/repositories/snapshots/",
    resolvers += "Mnemotix Nexus (releases)" at "http://dev.mnemotix.com:8080/nexus/content/repositories/releases/",
    resolvers += "Mnemotix Nexus (3rd party)" at "http://dev.mnemotix.com:8080/nexus/content/repositories/thirdparty/",
    resolvers += "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository"
  )

}
