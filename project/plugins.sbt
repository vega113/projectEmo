addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.20")

addSbtPlugin("io.gatling" % "gatling-sbt" % "4.6.0")

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

libraryDependencies += "com.spotify" % "docker-client" % "8.16.0"

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.10.0")