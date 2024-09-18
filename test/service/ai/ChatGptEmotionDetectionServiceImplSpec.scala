package service.ai

import dao.model.EmotionDetectionResult
import mockws.{MockWS, MockWSHelpers}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.Results._
import play.api.test.Helpers._
import service.model.DetectEmotionRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, _}

class ChatGptEmotionDetectionServiceImplSpec extends AnyFlatSpec with Matchers with ScalaFutures with MockitoSugar with MockWSHelpers with BeforeAndAfterAll {

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  trait Builder {
    val config: Configuration = mock[Configuration]
    doReturn("http://localhost:9000").when(config).get[String]("openai.baseUrl")
    doReturn(5.seconds).when(config).get[Duration]("openai.timeout")
  }

  override def afterAll(): Unit = {
    shutdownHelpers()
  }
}
