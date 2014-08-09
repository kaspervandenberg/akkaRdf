name := "akkaRdf"

version := "1.0"

scalaVersion := "2.11.1"

scalacOptions ++= Seq("-encoding", "UTF-8", "-target:jvm-1.7", "-feature")

autoAPIMappings := true

resolvers += "Gamlor-Repo" at "https://github.com/gamlerhart/gamlor-mvn/raw/master/snapshots"

val akkaVersion = "2.3.4"

val scalaVsn = "2.11.1"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.4"

apiMappings ++= {
  def findManagedDependency(organization: String, name: String): Option[File] = {
    (for {
      entry <- (fullClasspath in Runtime).value ++ (fullClasspath in Test).value
      module <- entry.get(moduleID.key) if module.organization == organization && module.name.startsWith(name)
    } yield entry.data).headOption
  }
  val links = Seq(
    findManagedDependency("org.scala-lang", "scala-library").map(d => d -> url(s"http://www.scala-lang.org/api/$scalaVsn/")),
    findManagedDependency("com.typesafe.akka", "akka-actor").map(d => d -> url(s"http://doc.akka.io/api/akka/$akkaVersion/")),
		findManagedDependency("com.typesafe", "config").map(d => d -> url("http://typesafehub.github.io/config/latest/api/")) //,
/*
		findManagedDependency("com.fasterxml.jackson.core", "jackson-core").map(d => d -> url("http://fasterxml.github.io/jackson-core/javadoc/2.3.1/")),
    findManagedDependency("io.spray", "spray-http").map(d => d -> url("http://spray.io/documentation/1.1-SNAPSHOT/api/")),
    findManagedDependency("io.spray", "spray-routing").map(d => d -> url("http://spray.io/documentation/1.1-SNAPSHOT/api/")),
    findManagedDependency("org.slf4j", "slf4j-api").map(d => d -> url("http://www.slf4j.org/api/")),
    findManagedDependency("com.typesafe.akka", "akka-testkit").map(d => d -> url(s"http://doc.akka.io/api/akka/$akkaVersion/")),
    findManagedDependency("org.specs2", "specs2").map(d => d -> url(s"http://etorreborre.github.io/specs2/api/SPECS2-$specs2Version/"))
*/
  )
  links.collect { case Some(d) => d }.toMap
}

val asyncDatabaseDependencies = Seq(
	"info.gamlor.akkaasync"  % "akka-dbclient_2.9.1" % "1.0-SNAPSHOT" exclude( "com.typesafe.akka", "akka-actor"),
	"info.gamlor.adbcj" % "scala-adbcj_2.10" % "0.7.2-SNAPSHOT",
	"org.adbcj" % "adbcj-api" % "0.7.2-SNAPSHOT",
	"org.adbcj" % "mysql-async-driver" % "0.7.2-SNAPSHOT"
)

