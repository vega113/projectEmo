import io.gatling.commons.validation.Success
import io.gatling.core.Predef.{Session, _}
import io.gatling.core.body.Body
import io.gatling.core.session.Expression
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder
import play.api.libs.json.{JsValue, Json}


object Requests {

  def extractUserIdFromJwtToken(jwtToken: String): Option[String] = {
    println("jwtToken: " + jwtToken)
    val jwtTokenParts = jwtToken.split('.')
    if (jwtTokenParts.length < 2) return None

    val jwtTokenPayload = jwtTokenParts(1)
    val jwtTokenPayloadDecoded = new String(java.util.Base64.getDecoder.decode(jwtTokenPayload))

    val json: JsValue = Json.parse(jwtTokenPayloadDecoded)
    (json \ "userId").asOpt[String]
  }

  def createEmoBodyStr(userId: String): String =
    s"""
       |{"id":100,"emotionType":"Positive","userId":$userId,"emotion":{"id":"Interest","emotionName":"Interest","emotionType":"Positive"},"intensity":3,"subEmotions":[{"subEmotionId":"Engagement","subEmotionName":"Engagement","parentEmotionId":"Interest"}],"triggers":[{"triggerId":9,"triggerName":"Situations","description":"Situations","created":"2023-03-31T07:13:18"}],"notes":[{"id":117,"title":"create a test emo","text":"create a test emo","description":"","suggestion":"","isDeleted":false,"lastUpdated":"2023-11-06T12:28:32"}],"tags":[],"created":"2023-11-06T11:36:12"}
       |""".stripMargin

  def createEmotionRecordBody(session: Session): HttpRequestBuilder = {
    val userId = session("userId").as[String]
    val bodyStr = createEmoBodyStr(userId)
    val bodyExpression: Expression[String] = _ => Success(bodyStr)
    val body: Body = StringBody(bodyExpression)

    http("Create Emotion Record")
      .post("/api/emotionRecord")
      .body(body).asJson // Here, explicitly use 'body' which is of type 'Body'
      .header("Authorization", "Bearer " + session("authToken").as[String])
      .check(status.is(200))
  }

}
