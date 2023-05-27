// Docker settings
dockerBaseImage := "openjdk:8-jdk"
dockerExposedPorts := Seq(9000)
dockerUpdateLatest := true

Universal / javaOptions ++= Seq(
  "-J-Xmx2g",
  "-Dpidfile.path=/dev/null"
)

Universal / mappings  += {
  // Create a new directory in the Docker image
  val directory = new java.io.File("./logs")
  directory -> "logs"
}


import com.typesafe.sbt.packager.docker.DockerChmodType

dockerAdditionalPermissions += (DockerChmodType.UserGroupPlusExecute, "/opt/docker/logs")


