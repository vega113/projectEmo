addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.19")

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

libraryDependencies += "com.spotify" % "docker-client" % "8.16.0"