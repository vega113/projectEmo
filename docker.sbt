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

Docker / defaultLinuxLogsLocation := "/opt/docker/logs"
dockerExposedVolumes := Seq((Docker / defaultLinuxLogsLocation).value)
dockerEnvVars := Map(
  "LOG_DIR" -> (Docker / defaultLinuxLogsLocation).value,
)

dockerCommands ++= Seq(
  Cmd("USER", "root"),
  Cmd("RUN", "mkdir -p /opt/docker/logs && chown -R 1001:root /opt/docker/logs"),
  Cmd("RUN", "chown 1001:root /opt/docker"),
  Cmd("USER", "1001"),
)

// Modify the ENTRYPOINT
//bashScriptExtraDefines += """exec "$@" &"""
//bashScriptExtraDefines += """touch /opt/docker/logs/application.log"""
//bashScriptExtraDefines += """tail -F /opt/docker/logs/application.log"""






