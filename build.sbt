
name := """architecture"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala).
  enablePlugins(SbtWeb)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0-RC1" % Test
)


libraryDependencies ++= {
  val scalaXmlV = "1.0.4"
  val akkaV = "2.4.7"
  val playSlickV = "2.0.0"
  val nscalaTimeV = "2.0.0"
  val codecV = "1.9"
  val mysqlConnectorV = "5.1.31"
  val slickV = "3.1.0"
  val httpclientVersion = "4.3.5"
  val httpcoreVersion = "4.3.2"
  val javaMailVersion = "1.5.3"
  val postgresql = "9.4.1208"

  Seq(
    "com.typesafe.play" %% "play-slick" % playSlickV,
    "com.typesafe.slick" %% "slick" % slickV withSources(),
    "com.typesafe.slick" %% "slick-codegen" % slickV,
    "com.typesafe.akka" %% "akka-remote" % akkaV,
    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
    "com.github.nscala-time" %% "nscala-time" % nscalaTimeV,
    "commons-codec" % "commons-codec" % codecV,
    "org.apache.httpcomponents" % "httpclient" % httpclientVersion withSources(),
    "org.apache.httpcomponents" % "httpcore" % httpcoreVersion withSources(),
    "org.apache.httpcomponents" % "httpmime" % httpclientVersion withSources(),
    "org.apache.commons" % "commons-collections4" % "4.0",
    "commons-io" % "commons-io" % "2.4",
    "org.dom4j" % "dom4j" % "2.0.0",
    "org.postgresql" % "postgresql" % postgresql
  )
}

// frontend
libraryDependencies ++= Seq(
  "org.webjars" % "webjars-play_2.11" % "2.4.0-1",
  "org.webjars" % "bootstrap" % "3.3.5",
  "org.webjars" % "react" % "0.13.3",
  "org.webjars.bower" % "react-router" % "0.13.3",
  "org.webjars.bower" % "reflux" % "0.2.11"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers += "sonatype-forge" at "https://repository.sonatype.org/content/groups/forge/"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
//routesGenerator := InjectedRoutesGenerator



pipelineStages := Seq(digest, gzip)