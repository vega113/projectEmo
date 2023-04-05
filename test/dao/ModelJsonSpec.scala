package dao

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import dao.model._
import play.api.libs.json.Json

import java.time.LocalDateTime

class ModelJsonSpec extends AnyFlatSpec with Matchers {

  "User" should "serialize and deserialize correctly" in {
    val user = User(None, "example_user2", "password123", Option("John"), Option("Doe"), "test@example.com", isPasswordHashed = false, None)
    val json = Json.toJson(user)
    val deserializedUser = json.as[User]
    deserializedUser shouldBe user
  }

  it should "deserialize correctly existing json string" in {
    val jsonStr =
      """
        |{
        |  "username": "example_user2",
        |  "password": "secure_password",
        |  "firstName": "John",
        |  "lastName": "Doe",
        |  "email": "example2@email.com",
        |  "isPasswordHashed": false
        |}
        |""".stripMargin
        val json = Json.parse(jsonStr)
    val deserializedUser = json.as[User]

    deserializedUser.username shouldBe "example_user2"
    deserializedUser.email shouldBe "example2@email.com"
    deserializedUser.password shouldBe "secure_password"
    deserializedUser.firstName shouldBe Option("John")
    deserializedUser.lastName shouldBe Option("Doe")
    deserializedUser.isPasswordHashed shouldBe false
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
    val triggers = List(Trigger(Option(1), "Person", Some(1), Some(1), Some("Listening to music")))
    val emotionRecord = EmotionRecord(Option(1), 1, "Joy", 5, subEmotions, triggers)

    val json = Json.toJson(emotionRecord)
    println("emotionRecordWithRelations:" + json.toString())
    val deserializedEmotionRecord = json.as[EmotionRecord]
    deserializedEmotionRecord shouldBe emotionRecord
  }

  it should "deserialize correctly existing json" in {
    val json = Json.parse(
      """
        |{
        |  "emotionRecord": {
        |    "id": 1,
        |    "userId": 1,
        |    "emotionId": "Joy",
        |    "intensity": 5,
        |    "subEmotions": [
        |    {
        |      "id": "Amusement",
        |      "subEmotionName": "Amusement",
        |      "emotionId": "Joy"
        |    },
        |    {
        |      "id": "Amusement",
        |      "subEmotionName": "Charm",
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
        |  }
        |}
        |""".stripMargin)

    val deserializedEmotionRecord = json.as[EmotionRecord]
    deserializedEmotionRecord shouldNot be(null)
  }
}

