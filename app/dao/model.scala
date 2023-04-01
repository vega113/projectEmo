package dao

import anorm.RowParser
import play.api.libs.json.{Format, Json}

import java.time.LocalDateTime

import anorm._
import anorm.SqlParser._

// refactor the code, make created field optional
object model {
  case class User(id: Int, username: String, email: String, password: String)

  case class Emotion(id: String, emotionName: String, emotionType: String)

  case class SubEmotion(id: String, subEmotionName: String, emotionId: String)

  case class EmotionRecord(id: Int, userId: String, emotionId: String, intensity: Int)

  case class Trigger(id: Int, triggerName: String, parentId: Option[Int], userId: Option[Int], description: Option[String])

  case class Note(id: Int, title: String, content: String, userId: Option[Int])

  case class Tag(id: Int, userId: Option[Int], tagName: String)

  case class EmotionRecordTag(emotionRecordId: Int, tagId: Int)

  case class NoteTag(noteId: Int, tagId: Int)

  case class EmotionRecordWithRelations(emotionRecord: EmotionRecord, subEmotions: List[SubEmotion], triggers: List[Trigger])


  object User {
    implicit val userFormat: Format[User] = Json.format[User]

    val parser: RowParser[User] = {
      int("id") ~
        str("username") ~
        str("email") ~
        str("password") map {
        case id ~ username ~ email ~ password =>
          User(id, username, email, password)
      }
    }
  }

  object Emotion {
    implicit val emotionFormat: Format[Emotion] = Json.format[Emotion]

    val parser: RowParser[Emotion] = {
      str("id") ~
        str("emotion_name") ~
        str("emotion_type") map {
        case id ~ emotionName ~ emotionType =>
          Emotion(id, emotionName, emotionType)
      }
    }
  }

  object SubEmotion {
    implicit val subEmotionFormat: Format[SubEmotion] = Json.format[SubEmotion]

    // update column names to be kebab case
    val parser: RowParser[SubEmotion] = {
      str("id") ~
        str("sub_emotion_name") ~
        str("emotion_id") map {
        case id ~ subEmotionName ~ emotionId =>
          SubEmotion(id, subEmotionName, emotionId)
      }
    }
  }

  object EmotionRecord {
    implicit val emotionRecordFormat: Format[EmotionRecord] = Json.format[EmotionRecord]

    val parser: RowParser[EmotionRecord] = {
      int("id") ~
        str("user_id") ~
        str("emotion_id") ~
        int("intensity") map {
        case id ~ userId ~ emotionId  ~ intensity =>
          EmotionRecord(id, userId, emotionId, intensity)
      }
    }
  }

  object Trigger {
    implicit val triggerFormat: Format[Trigger] = Json.format[Trigger]

    val parser: RowParser[Trigger] = {
      int("id") ~
        str("trigger_name") ~
        int("user_id") ~
        int("parent_id") ~
        str("description") map {
        case id ~ triggerName ~ parentId ~ userId ~ description =>
          Trigger(id, triggerName, Option(parentId), Option(userId), Option(description))
      }
    }
  }

  object Note {
    implicit val noteFormat: Format[Note] = Json.format[Note]

    val parser: RowParser[Note] = {
      int("id") ~
        str("title") ~
        str("content") ~
        int("userId")  map {
        case id ~ title ~ content ~ userId =>
          Note(id, title, content, Some(userId))
      }
    }
  }

  object Tag {
    implicit val tagFormat: Format[Tag] = Json.format[Tag]

    val parser: RowParser[Tag] = {
      int("id") ~
        int("userId") ~
        str("tagName") map {
        case id ~ userId ~ tagName =>
          Tag(id, Some(userId), tagName)
      }
    }
  }

  object EmotionRecordWithRelations {
    implicit val emotionRecordWithRelationsFormat: Format[EmotionRecordWithRelations] = Json.format[EmotionRecordWithRelations]
    val parser: RowParser[EmotionRecordWithRelations] = {
      EmotionRecord.parser ~ SubEmotion.parser ~ Trigger.parser map {
        case emotionRecord ~ subEmotion ~ trigger => EmotionRecordWithRelations(emotionRecord, List(subEmotion), List(trigger))
      }
    }
  }
}
