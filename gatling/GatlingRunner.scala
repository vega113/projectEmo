import io.gatling.app.Gatling
import io.gatling.core.config.GatlingPropertiesBuilder

object GatlingRunner {
  def main(args: Array[String]): Unit = {
    // Create a Gatling configuration
    val props = new GatlingPropertiesBuilder
    // Point Gatling to the class containing the simulation
    props.simulationClass(classOf[UserSimulation].getName)
    // Run Gatling with these properties
    Gatling.fromMap(props.build)
  }
}
