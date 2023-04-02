package dao

import anorm.RowParser
import play.api.libs.json.{Format, Json}

import java.time.LocalDateTime

import anorm._
import anorm.SqlParser._

// refactor the code, make created field optional
object model {
  case class User(id: Option[Int], username: String, email: String, password: String)

  case class Emotion(id: String, emotionName: String, emotionType: String)

  case class SubEmotion(subEmotionId: String, subEmotionName: String, parentEmotionId: String)

  case class EmotionRecord(id: Option[Int], userId: Int, emotionId: String, intensity: Int)

  case class Trigger(triggerId: Option[Int], triggerName: String, parentId: Option[Int], createdByUser: Option[Int], description: Option[String])

  case class Note(id: Option[Int], title: String, content: String, userId: Option[Int])

  case class Tag(id: Option[Int], userId: Option[Int], tagName: String)

  case class EmotionRecordTag(emotionRecordId: Int, tagId: Int)

  case class NoteTag(noteId: Int, tagId: Int)

  case class EmotionRecordWithRelations(emotionRecord: EmotionRecord, subEmotions: List[SubEmotion], triggers: List[Trigger])


  object User {
    implicit val userFormat: Format[User] = Json.format[User]
    val parser: RowParser[User] = Macro.namedParser[User]
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
      str("sub_emotion_id") ~
        str("sub_emotion_name") ~
        str("parent_emotion_id") map {
        case subEmotionId ~ subEmotionName ~ parentEmotionId =>
          SubEmotion(subEmotionId, subEmotionName, parentEmotionId)
      }
    }
  }

  object EmotionRecord {
    implicit val emotionRecordFormat: Format[EmotionRecord] = Json.format[EmotionRecord]

    val parser: RowParser[EmotionRecord] = {
      get[Option[Int]]("id") ~
        int("user_id") ~
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
      get[Option[Int]]("trigger_id") ~
        str("trigger_name") ~
        get[Option[Int]]("created_by_user") ~
        get[Option[Int]]("parent_id") ~
        str("description") map {
        case triggerId ~ triggerName ~ parentId ~ userId ~ description =>
          Trigger(triggerId, triggerName, parentId, userId, Option(description))
      }
    }
  }

  object Note {
    implicit val noteFormat: Format[Note] = Json.format[Note]

    val parser: RowParser[Note] = {
      get[Option[Int]]("id") ~
        str("title") ~
        str("content") ~
        int("user_id")  map {
        case id ~ title ~ content ~ userId =>
          Note(id, title, content, Some(userId))
      }
    }
  }

  object Tag {
    implicit val tagFormat: Format[Tag] = Json.format[Tag]

    val parser: RowParser[Tag] = {
      get[Option[Int]]("id") ~
        int("user_id") ~
        str("tag_name") map {
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
