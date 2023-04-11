package controllers

import akka.util.Timeout
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.http.Status.OK
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, status, stubControllerComponents}
import service.{EmotionRecordService, UserService}
import service.EmotionRecordService

class EmotionRecordControllerSpec extends PlaySpec with MockitoSugar {
  private val mockEmotionRecordService = mock[EmotionRecordService]

  trait TestData {
    implicit val timeout = Timeout(1.second)
  }

  "EmotionRecordController" should {
    "return a list of all emotion records" in new TestData {
//      val controller = new EmotionRecordController(stubControllerComponents(), mockEmotionRecordService)
//      val fakeRequest = FakeRequest(GET, "/emotion-records")
//      val emotionRecords = controller.findAll().apply(fakeRequest)
//      status(emotionRecords) mustEqual OK
    }
  }
}
