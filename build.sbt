ThisBuild / scalaVersion := "2.13.10"

ThisBuild / version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """projectEmo""",
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
    )
  )

resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += "Typesafe Simple Repository" at "https://repo.typesafe.com/typesafe/simple/maven-releases/"

libraryDependencies ++= Seq(
  jdbc,
  "org.playframework.anorm" %% "anorm" % "2.7.0",
  "mysql" % "mysql-connector-java" % "8.0.27",
  "com.zaxxer" % "HikariCP" % "4.0.3",

)
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.8.1"

libraryDependencies += "org.liquibase" % "liquibase-core" % "4.6.2"

