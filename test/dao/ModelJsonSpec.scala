package dao

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import dao.model._
import play.api.libs.json.Json
import uitil.TestObjectsFactory.createEmotionRecord

import java.time.LocalDateTime

class ModelJsonSpec extends AnyFlatSpec with Matchers {

  "User" should "serialize and deserialize correctly" in {
    val user = User(None, "example_user2", "password123", Option("John"), Option("Doe"), "test@example.com", isPasswordHashed = Option(false), None)
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
    deserializedUser.isPasswordHashed shouldBe Some(false)
  }

  "Emotion" should "serialize and deserialize correctly" in {
    val emotion = Emotion(Some("1"), Option("Happiness"), "Positive")
    val json = Json.toJson(emotion)
    val deserializedEmotion = json.as[Emotion]
    deserializedEmotion shouldBe emotion
  }

  // Similar tests for other case classes

  "EmotionRecord" should "serialize and deserialize correctly" in {
    val emotionRecord = createEmotionRecord()
    val json = Json.toJson(emotionRecord)
    println("emotionRecordWithRelations:" + json.toString())
    val deserializedEmotionRecord = json.as[EmotionRecord]
    deserializedEmotionRecord shouldBe emotionRecord
  }

  it should "deserialize correctly existing json" in {
    val json = Json.parse(
      """
        |{
        |  "id": 1,
        |  "userId": 1,
        |  "emotion": {
        |    "id": "Joy",
        |    "emotionType": "Positive",
        |    "emotionName": "Joy",
        |    "description": "description"
        |  },
        |  "intensity": 5,
        |  "subEmotions": [
        |    {
        |      "subEmotionId": "Amusement",
        |      "subEmotionName": "Amusement",
        |      "parentEmotionId": "Joy"
        |    },
        |    {
        |      "subEmotionId": "Charm",
        |      "subEmotionName": "Charm",
        |      "parentEmotionId": "Joy"
        |    }
        |  ],
        |  "triggers": [
        |    {
        |      "triggerId": 1,
        |      "triggerName": "Person",
        |      "parentId": 1,
        |      "createdByUser": 1,
        |      "description": "Listening to music"
        |    }
        |  ],
        |  "notes": [
        |    {
        |      "id": 1,
        |      "title": "Note 1",
        |      "text": "Note 1 description"
        |    }
        |  ],
        |  "tags": [
        |    {
        |      "tagId": 1,
        |      "tagName": "Tag 1"
        |    }
        |  ]
        |}
        |""".stripMargin)
    println("json:" + json.toString())
    val deserializedEmotionRecord = json.as[EmotionRecord]
    println("deserializedEmotionRecord:" + deserializedEmotionRecord.toString)
    deserializedEmotionRecord shouldNot be(null)
    deserializedEmotionRecord.subEmotions shouldNot be(null)
    deserializedEmotionRecord.subEmotions.size shouldBe 2
    deserializedEmotionRecord.subEmotions.head.subEmotionId shouldBe Option("Amusement")
    deserializedEmotionRecord.subEmotions.head.subEmotionName shouldBe Option("Amusement")
    deserializedEmotionRecord.subEmotions.head.parentEmotionId shouldBe Option("Joy")
    deserializedEmotionRecord.subEmotions(1).subEmotionId shouldBe Option("Charm")
    deserializedEmotionRecord.subEmotions(1).subEmotionName shouldBe Option("Charm")
    deserializedEmotionRecord.subEmotions(1).parentEmotionId shouldBe Option("Joy")
    deserializedEmotionRecord.triggers shouldNot be(null)
    deserializedEmotionRecord.triggers.size shouldBe 1
    deserializedEmotionRecord.triggers.head.triggerId shouldBe Option(1)
    deserializedEmotionRecord.triggers.head.triggerName shouldBe Option("Person")
    deserializedEmotionRecord.triggers.head.parentId shouldBe Option(1)
    deserializedEmotionRecord.triggers.head.createdByUser shouldBe Option(1)
    deserializedEmotionRecord.triggers.head.description shouldBe Option("Listening to music")
  }
}

