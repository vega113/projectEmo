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



  "makeApiCall response" should "correctly deserialize the response" in new Builder {
    val wsClient: WSClient = MockWS { case (POST, "http://localhost:9000/v1/chat/completions") =>
      Action { request =>
        Ok(Json.parse(
          """
            |{"id":"chatcmpl-1a666760-19f6-4943-a23b-2cf2006db7fa","object":"chat.completion","created":1716101230,"model":"llama3-8b-8192","choices":[{"index":0,"message":{"role":"assistant","content":"Here's the JSON output for the given text:\n\n{\"textTitle\": \"Testing Ollama 3 AI Model for Emotion\", \"intensity\": 3, \"subEmotionId\": \"Curiosity\", \"description\": \"You're excited to test the Ollama 3 AI model for emotions!\", \"suggestion\": \"Explore more about Ollama 3's capabilities and how it can be used in various applications.\", \"triggers\": [{\"triggerName\": \"Ideas\"}], \"tags\": [{\"tagName\": \"ArtificialIntelligence\"}, {\"tagName\": \"Emotions\"}], \"todos\": [{\"title\": \"Test the Ollama 3 AI model for different scenarios\"}, {\"title\": \"Explore Ollama 3's documentation and tutorials\"}], \"tags\": []}"},"logprobs":null,"finish_reason":"stop"}],"usage":{"prompt_tokens":1372,"prompt_time":0.348,"completion_tokens":163,"completion_time":0.196,"total_tokens":1535,"total_time":0.544},"system_fingerprint":"fp_dadc9d6142","x_groq":{"id":"req_01hy7snb92e2x9e9zwg4fan0gy"}}
            |""".stripMargin))
      }
    }

    val service = new ChatGptEmotionDetectionServiceImpl(wsClient, config)
    val result: EmotionDetectionResult = service.makeApiCall(DetectEmotionRequest("testing emo app with groq again", 1)).futureValue

    result.intensity shouldBe 3
  }
}
