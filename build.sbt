ThisBuild / scalaVersion := "2.13.10"

ThisBuild / version := "1.0.3.8"

maintainer := "vega113@gmail.com"

enablePlugins(JavaAppPackaging)

javaOptions ++= Seq(
  "-Dgraal.CompilationFailureAction=Silent",
  "-Xms150m",
  "-Xmx300m",
  "-Xss1M",
  "-XX:+CMSClassUnloadingEnabled"
)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, PlayJava, GatlingPlugin, BuildInfoPlugin)
  .settings(
    name := "projectEmo",
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
    ),
    watchSources ++= (baseDirectory.value / "ui/emo-app/src" ** "*").get,
    inConfig(GatlingIt)(Defaults.testSettings),
    buildInfoKeys := Seq[BuildInfoKey](
      version,
      "buildTimestamp" -> new java.util.Date(System.currentTimeMillis()),
    ),
  )


GatlingIt / resourceDirectory := baseDirectory.value / "gatling/resources"
GatlingIt / scalaSource := baseDirectory.value / "gatling"
GatlingIt / javaOptions := overrideDefaultJavaOptions("-Xms1024m", "-Xmx2048m")


resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += "Typesafe Simple Repository" at "https://repo.typesafe.com/typesafe/simple/maven-releases/"
resolvers += "Atlassian's Maven Public Repository" at "https://packages.atlassian.com/maven-public/"
resolvers += Resolver.jcenterRepo

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.15.1"
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.15.1"

libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.2"
dependencyOverrides += "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.2"



libraryDependencies ++= Seq(
  jdbc,
  "org.playframework.anorm" %% "anorm" % "2.7.0",
  "mysql" % "mysql-connector-java" % "8.0.33",
  "com.zaxxer" % "HikariCP" % "5.0.1",

)
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.4"

libraryDependencies += "org.liquibase" % "liquibase-core" % "4.20.0"

libraryDependencies += "com.pauldijou" %% "jwt-core" % "5.0.0"
libraryDependencies += "com.pauldijou" %% "jwt-play-json" % "5.0.0"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.8"
libraryDependencies += "org.fusesource.jansi" % "jansi" % "2.4.0"
libraryDependencies += "com.google.inject" % "guice" % "5.1.0"

libraryDependencies += ws

libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "7.4"

dependencyOverrides += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.3.0"

libraryDependencies += "io.honeybadger" % "honeybadger-java" % "2.1.2"

libraryDependencies += "io.github.sashirestela" % "simple-openai" % "3.5.0"

libraryDependencies += "io.cequence" %% "openai-scala-client" % "1.0.0"


libraryDependencies ++= Seq(
  "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.9.5",
  "io.gatling"            % "gatling-test-framework"    % "3.9.5"
)

libraryDependencies += "org.mockito" %% "mockito-scala" % "1.17.31" % Test
libraryDependencies += "org.scalatestplus" %% "scalatestplus-mockito" % "1.0.0-M2" % Test

libraryDependencies += "de.leanovate.play-mockws" %% "play-mockws-2-8" % "3.0.1" % Test











