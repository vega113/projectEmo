package dao

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import dao.model._
import play.api.libs.json.Json

import java.time.LocalDateTime

class ModelJsonSpec extends AnyFlatSpec with Matchers {

  "User" should "serialize and deserialize correctly" in {
    val user = User(Option(1), "testUser", "password123", Option("John"), Option("Doe"), "test@example.com", false, LocalDateTime.now())



    val json = Json.toJson(user)
    val deserializedUser = json.as[User]
    deserializedUser shouldBe user
  }

  "Emotion" should "serialize and deserialize correctly" in {
    val emotion = Emotion("1", "Happiness", "Positive")
    val json = Json.toJson(emotion)
    val deserializedEmotion = json.as[Emotion]
    deserializedEmotion shouldBe emotion
  }

  // Similar tests for other case classes

  "EmotionRecord" should "serialize and deserialize correctly" in {
    val subEmotions = List(SubEmotion(Option("Amusement"), "Amusement", "Joy"))
    val triggers = List(Trigger(Option(1), "Person", Some(1), Some(1), Some("Listening to music"), LocalDateTime.now()))
    val emotionRecord = EmotionRecord(Option(1), 1, "Joy", 5, subEmotions, triggers, LocalDateTime.now())

    val emotionRecordWithRelations = EmotionRecord(Option(1), 1, "Joy", 5, subEmotions, triggers, LocalDateTime.now())
    val json = Json.toJson(emotionRecordWithRelations)
    println("emotionRecordWithRelations:" + json.toString())
    val deserializedEmotionRecord = json.as[EmotionRecord]
    deserializedEmotionRecord shouldBe emotionRecordWithRelations
  }

  it should "deserialize correctly existing json" in {
    val json = Json.parse(
      """
        |{
        |  "emotionRecord": {
        |    "id": 1,
        |    "userId": 1,
        |    "emotionId": "Joy",
        |    "intensity": 5
        |  },
        |  "subEmotions": [
        |    {
        |      "id": "Amusement",
        |      "subEmotionName": "Amusement",
        |      "emotionId": "Joy"
        |    }
        |  ],
        |  "triggers": [
        |    {
        |      "id": 1,
        |      "triggerName": "Person",
        |      "parentId": 1,
        |      "userId": 1,
        |      "description": "Listening to music"
        |    }
        |  ]
        |}
        |""".stripMargin)

    val deserializedEmotionRecord = json.as[EmotionRecord]
    deserializedEmotionRecord shouldNot(be(null))
  }
}

