
enablePlugins(DockerPlugin)
// Docker settings
dockerBaseImage := "openjdk:8-alpine"
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
//
dockerCommands ++= Seq(
  Cmd("USER", "root"),
  Cmd("RUN", "apk add --no-cache bash"),
  Cmd("USER", "1001"),

)

//dockerCommands := {
//  val commands = dockerCommands.value
//  val index = commands.indexWhere {
//    case Cmd("RUN", args @ _*) => args.contains("demiourgos728")
//    case _ => false
//  }
//  commands.patch(index, Seq(ExecCmd("RUN", "yum", "-y", "install", "shadow-utils")), 0)
//}





