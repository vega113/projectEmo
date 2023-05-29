// Docker settings
dockerBaseImage := "amazoncorretto:8-alpine-jdk"
dockerExposedPorts := Seq(9000)
dockerUpdateLatest := true

Universal / javaOptions ++= Seq(
  "-J-Xmx2g",
  "-Dpidfile.path=/dev/null"
)

import com.typesafe.sbt.packager.docker.Cmd
dockerCommands ++= Seq(
  Cmd("USER", "root"),
  Cmd("RUN", "apk add --no-cache mysql-client"),
  Cmd("USER", "1001")
)

//Docker / dockerCommands := Seq(
//  Cmd("FROM", dockerBaseImage.value),
//  Cmd("RUN", "apk add --no-cache mysql-client"),
//  Cmd("WORKDIR", "/opt/docker"),
//  Cmd("COPY", "target/universal/stage/ ."),
//  Cmd("RUN", "chmod +x bin/projectemo"),
//  Cmd("RUN", "mkdir logs"),
//  Cmd("ENTRYPOINT", "bin/projectemo")
//)





