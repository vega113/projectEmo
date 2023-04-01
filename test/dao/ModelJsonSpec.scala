package dao

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import dao.model._
import play.api.libs.json.Json

import java.time.LocalDateTime

class ModelJsonSpec extends AnyFlatSpec with Matchers {

  "User" should "serialize and deserialize correctly" in {
    val user = User(1, "testUser", "test@example.com", "password123")
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

  "EmotionRecordWithRelations" should "serialize and deserialize correctly" in {
    val emotionRecord = EmotionRecord(1, "1", "Joy", 5)
    val subEmotions = List(SubEmotion("Amusement", "Amusement", "Joy"))
    val triggers = List(Trigger(1, "Person", Some(1), Some(1), Some("Listening to music")))

    val emotionRecordWithRelations = EmotionRecordWithRelations(emotionRecord, subEmotions, triggers)
    val json = Json.toJson(emotionRecordWithRelations)
    println("emotionRecordWithRelations:" + json.toString())
    val deserializedEmotionRecordWithRelations = json.as[EmotionRecordWithRelations]
    deserializedEmotionRecordWithRelations shouldBe emotionRecordWithRelations
  }

  it should "deserialize correctly existing json" in {
    val json = Json.parse(
      """
        |{
        |  "emotionRecord": {
        |    "id": 1,
        |    "userId": "1",
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

    val deserializedEmotionRecordWithRelations = json.as[EmotionRecordWithRelations]
    deserializedEmotionRecordWithRelations shouldNot(be(null))
  }
}

