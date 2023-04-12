ThisBuild / scalaVersion := "2.13.10"

ThisBuild / version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """projectEmo""",
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
    ),
    watchSources ++= (baseDirectory.value / "ui/emo-app/src" ** "*").get
  )


resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += "Typesafe Simple Repository" at "https://repo.typesafe.com/typesafe/simple/maven-releases/"
resolvers += "Atlassian's Maven Public Repository" at "https://packages.atlassian.com/maven-public/"
resolvers += Resolver.jcenterRepo

libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.2"
dependencyOverrides += "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.2"



libraryDependencies ++= Seq(
  jdbc,
  "org.playframework.anorm" %% "anorm" % "2.7.0",
  "mysql" % "mysql-connector-java" % "8.0.27",
  "com.zaxxer" % "HikariCP" % "4.0.3",

)
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.8.1"

libraryDependencies += "org.liquibase" % "liquibase-core" % "4.4.2"

libraryDependencies += "com.pauldijou" %% "jwt-core" % "5.0.0"
libraryDependencies += "com.pauldijou" %% "jwt-play-json" % "5.0.0"

libraryDependencies += "org.mockito" % "mockito-core" % "2.10.0" % "test"
libraryDependencies += "org.scalatestplus" %% "mockito-4-6" % "3.2.15.0" % "test"











