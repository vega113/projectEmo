import com.typesafe.sbt.packager.Keys.defaultLinuxLogsLocation

enablePlugins(DockerPlugin)

dockerBaseImage := "adoptopenjdk/openjdk11:jdk-11.0.8_10-ubuntu"
import com.typesafe.sbt.packager.docker.*

dockerExposedPorts := Seq(9000)
dockerUpdateLatest := true

Universal / javaOptions ++= Seq(
  "-J-Xmx2g",
  "-Dpidfile.path=/dev/null"
)

//
dockerCommands ++= Seq(
  Cmd("USER", "root"),
  Cmd("RUN", "mkdir -p /opt/docker/logs && chown -R 1001:root /opt/docker/logs"),
  Cmd("RUN", "chown 1001:root /opt/docker"),
  Cmd("USER", "1001"),

)






