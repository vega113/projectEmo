package service

import dao.model.EmotionDetectionResult
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json

class ModelJsonDeserializationSpec extends AnyFlatSpec with Matchers with ScalaFutures with MockitoSugar {

  "ModelJsonDeserialization" should "parse EmotionDetectionResult correctly using Groq Ollama3 model" in {
    val json =
      """
        |{
        |  "textTitle": "Let's test Ollama 3 as AI model for emosig!",
        |  "intensity": 3,
        |  "subEmotionId": "Curiosity",
        |  "description": "You're excited and interested in testing the AI model!",
        |  "suggestion": "Go ahead and see how well it performs!",
        |  "triggers": [
        |    {
        |      "triggerName": "Other"
        |    }
        |  ],
        |  "tags": [
        |    {
        |      "tagName": "Interest"
        |    },
        |    {
        |      "tagName": "AI"
        |    }
        |  ],
        |  "todos": [
        |    {
        |      "title": "Test Ollama 3",
        |      "description": "Try out the model and see how it performs",
        |      "type": "test"
        |    }
        |  ]
        |}
        |""".stripMargin
    val actual = Json.parse(json).as[EmotionDetectionResult]
    actual.textTitle.get shouldEqual "Let's test Ollama 3 as AI model for emosig!"
  }
}
