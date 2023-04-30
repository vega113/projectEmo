package controllers

import akka.stream.Materializer
import akka.util.Timeout
import auth.model.LoginData
import auth.{AuthenticatedAction, JwtService}
import dao.model._
import org.mockito.Mockito.when
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{POST, status, stubControllerComponents}
import service.{EmotionRecordService, UserService}

import scala.concurrent.Future

class EmotionRecordControllerSpec extends PlaySpec with MockitoSugar with GuiceOneAppPerSuite{
  private val mockEmotionRecordService = mock[EmotionRecordService]
  implicit lazy val materializer: Materializer = app.materializer


  trait TestData {
    implicit val timeout = Timeout(1.second)
    implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
    val mockUserService = mock[UserService]
    val mockJwtService = mock[JwtService]
    val authenticatedAction: AuthenticatedAction = new AuthenticatedAction(mockUserService, mockJwtService)
  }

  "EmotionRecordController" should {
    "insert emotion record" ignore new TestData {
      val loginData = LoginData("test-user", "test-password")
      val user = User(Some(1), "test-user", "test-password", Some("Test"), Some("User"), "testuser@test.com", isPasswordHashed = Some(true))
      val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3MTI3NzY5MzksImlhdCI6MTY4MTI0MDkzOSwidXNlcklkIjozLCJ1c2VybmFtZSI6IkZpZnR5b25lQWRtaW5Vc2VyMSIsImVtYWlsIjoiRmlmdHlvbmVBZG1pblVzZXIxQGVtYWlsLmNvbSIsImZpcnN0bmFtZSI6Ill1cmkiLCJsYXN0bmFtZSI6IlVzZXIiLCJyb2xlIjoidXNlciJ9.brtjzMVjEv_h_MiZkCjuexDovZFBkm-eYlQdSAXR1n4"
      when(mockUserService.findByUsername(loginData.username)).thenReturn(Future.successful(Some(user)))
      when(mockJwtService.createToken(user, 1.hour)).thenReturn(token)

      val json = Json.parse(
        """
          |{"userId":1,"emotion":{"id":"Surprise", "emotionName": "Surprise", "emotionType": "Positive", "description": "description"},"intensity":3,"subEmotions":[{"subEmotionId":"Astonishment"}],"triggers":[{"triggerId":2}]}
          |""".stripMargin)
      println("emotionRecordWithRelations from string:" + json.toString())

      val subEmotions = List(SubEmotion(Option("Amusement"), Option("Amusement"), Option("description"), Option("Joy")))
      val triggers = List(Trigger(Option(1), Some("Person"), Some(1), Some(1), Some("Listening to music")))
      val emotionRecord = EmotionRecord(None, Option(1L), Emotion("Joy", Option("Joy"), "Positive", Some("A nice emotion")), 5, subEmotions, triggers)

      val jsonFromObj = Json.toJson(emotionRecord)
      println("emotionRecordWithRelations from object:" + jsonFromObj.toString())

      val deserializedEmotionRecord = json.as[EmotionRecord]

      val controller = new EmotionRecordController(stubControllerComponents(), mockEmotionRecordService, authenticatedAction)
      val request = FakeRequest(POST, "/api/emotionRecord").withBody(deserializedEmotionRecord).
        withHeaders("Content-Type" -> "application/json", "Authorization" -> s"Bearer $token")
      val result = controller.insert().apply(request)
      status(result) mustBe OK

      // todo there's an issue with materializer, it requires a running app which runs liquibase. need to check how to mock it and then re-enable this test
    }
  }
}
