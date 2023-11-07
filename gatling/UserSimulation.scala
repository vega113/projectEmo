import io.gatling.core.Predef._
import io.gatling.core.feeder.Feeder
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import java.util.Base64
import scala.concurrent.duration._



class UserSimulation extends Simulation {

  val userFeeder: Feeder[String] = Iterator.continually(Map(
    "username" -> s"user_${java.util.UUID.randomUUID.toString}",
    "email" -> s"${java.util.UUID.randomUUID.toString}@example.com"
  ))

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl("http://localhost:4200")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val scn: ScenarioBuilder = scenario("User Operations")
    .feed(userFeeder)
    .exec(http("Create User")
      .post("/api/user")
      .body(StringBody("""{"username":"${username}","password":"testpass", "email":"${email}"}""")).asJson
      .check(status.in(201, 409)))
    .pause(1)
    .exec(http("Login")
      .post("/api/login")
      .body(StringBody("""{"username":"${username}","password":"testpass"}""")).asJson
      .check(jsonPath("$.token").saveAs("authToken"))
      .check(status.is(200)))
    .exec(http("Emotion Cache")
      .get("/api/emotionCache")
      .check(status.is(200)))
    .exec { session =>
      val authToken = session("authToken").as[String]
      val payload = parseJwtToken(authToken)
      val userId = extractUserId(payload)
      session.set("userId", userId)
    }
    .pause(1)
    .exec(http("Create Emotion Record")
      .post("/api/emotionRecord")
      .body(StringBody(Requests.createEmoBodyStr("${userId}"))) // Ensure Requests.createEmoBodyStr is defined elsewhere in your code
      .header("Authorization", "Bearer ${authToken}")
      .check(status.is(200)))
    .pause(1)
    .exec(http("Fetch Timeline")
      .get("/api/emotionRecord/user")
      .header("Authorization", "Bearer ${authToken}")
      .check(status.is(200)))
    .pause(1)
    .exec(http("View Calendar")
      .get("/api/emotionRecord/user/month/2023-11-01T00:00:00+02:00/2023-11-30T23:59:59+02:00")
      .header("Authorization", "Bearer ${authToken}")
      .check(status.is(200)))
    .pause(1)
    .exec(http("View Charts Doughnut")
      .get("/api/charts/user/doughnut/emotionTypesTrigger/month/2023-08-07T07:08:41Z/2023-11-07T07:08:41Z")
      .header("Authorization", "Bearer ${authToken}")
      .check(status.is(200)))
    .pause(1)
    .exec(http("View Charts Line")
      .get("/api/emotionRecord/day/user/month/2023-08-07T07:08:41Z/2023-11-07T07:08:41Z")
      .header("Authorization", "Bearer ${authToken}")
      .check(status.is(200)))
    .pause(1)
    .exec(http("Logout")
      .post("/api/logout") // Assuming this is the endpoint to logout
      .header("Authorization", "Bearer ${authToken}") // If logout requires a token
      .check(status.is(200)))

  setUp(
    scn.inject(
      nothingFor(1.seconds),
      atOnceUsers(10),
      rampUsers(50).during(1.minutes) // Ramp up to 50 additional users over the period of 5 minutes
    ).protocols(httpProtocol)
  )

  def parseJwtToken(token: String): String = {
    val parts = token.split("\\.")
    val payload = new String(Base64.getDecoder.decode(parts(1)))
    payload
  }

  def extractUserId(payload: String): String = {
    // Assuming payload is a simple JSON like {"userId":5, ...}
    val userIdPattern = """"userId":(\d+)""".r
    userIdPattern.findFirstMatchIn(payload) match {
      case Some(m) => m.group(1)
      case None => throw new Exception("UserId not found in JWT payload")
    }
  }
}