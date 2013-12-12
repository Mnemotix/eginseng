import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "eginsengServer"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    javaJdbc,
    javaEbean,
    "org.apache.jena" % "apache-jena-libs" % "2.10.0",
    "fr.maatg.pandora.clients" % "pandora-clients-fedehr" % "1.0.7-SNAPSHOT"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
      
      
    // Add your own project settings here
   resolvers += "Apache Nexus" at "https://repository.apache.org/content/repositories/releases/",
   resolvers += "Maven Repository Switchboard" at "http://repo1.maven.org/maven2",
   resolvers += "maatG France Releases" at "http://nexus.maatg.fr/content/repositories/maatg-fr-releases",
   resolvers += "maatG France Snapshots" at "http://nexus.maatg.fr/content/repositories/maatg-fr-snapshots",
   resolvers += "Mnemotix Nexus (snapshots)" at "http://dev.mnemotix.com:8080/nexus/content/repositories/snapshots/",
	resolvers += "Mnemotix Nexus (releases)" at "http://dev.mnemotix.com:8080/nexus/content/repositories/releases/",
	resolvers += "Mnemotix Nexus (3rd party)" at "http://dev.mnemotix.com:8080/nexus/content/repositories/thirdparty/"
	//resolvers += "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository"
  
  )

}
