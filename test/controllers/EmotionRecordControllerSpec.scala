package controllers

import akka.stream.Materializer
import akka.util.Timeout
import auth.model.LoginData
import auth.{AuthenticatedAction, JwtService}
import dao.model.{EmotionRecord, User}
import org.mockito.Mockito.when
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, POST, status, stubControllerComponents}
import service.{EmotionRecordService, UserService}
import service.EmotionRecordService
import dao.model._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

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
      val token = "test-token"
      when(mockUserService.findByUsername(loginData.username)).thenReturn(Future.successful(Some(user)))
      when(mockJwtService.createToken(user, 1.hour)).thenReturn(token)

      val json = Json.parse(
        """
          |{"userId":4,"emotionId":"Surprise","intensity":3,"subEmotions":[{"subEmotionId":"Astonishment"}],"triggers":[{"triggerId":2}]}
          |""".stripMargin)
      println("emotionRecordWithRelations from string:" + json.toString())

      val subEmotions = List(SubEmotion(Option("Amusement"), Option("Amusement"), Option("Joy")))
      val triggers = List(Trigger(Option(1), Some("Person"), Some(1), Some(1), Some("Listening to music")))
      val emotionRecord = EmotionRecord(None, Option(1L), "Joy", 5, subEmotions, triggers)

      val jsonFromObj = Json.toJson(emotionRecord)
      println("emotionRecordWithRelations from object:" + jsonFromObj.toString())

      val deserializedEmotionRecord = json.as[EmotionRecord]

      val controller = new EmotionRecordController(stubControllerComponents(), mockEmotionRecordService, authenticatedAction)
      val request = FakeRequest(POST, "/api/emotionRecord").withBody(deserializedEmotionRecord).
        withHeaders("Content-Type" -> "application/json")
      val result = controller.insert().apply(request)
      status(result) mustBe OK

      // todo there's an issue with materializer, it requires a running app which runs liquibase. need to check how to moack it and then re-enable this test
    }
  }
}
