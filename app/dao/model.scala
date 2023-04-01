package dao

import anorm.RowParser
import play.api.libs.json.{Format, Json}

import java.time.LocalDateTime

import anorm._
import anorm.SqlParser._

// refactor the code, make created field optional
object model {
  case class User(id: Int, username: String, email: String, password: String, created: Option[LocalDateTime])

  case class Emotion(id: String, emotionName: String, emotionType: String, created: Option[LocalDateTime])

  case class SubEmotion(id: String, subEmotionName: String, emotionId: String, created: Option[LocalDateTime])

  case class EmotionRecord(id: Int, userId: String, emotionId: String, intensity: Int, created: Option[LocalDateTime])

  case class Trigger(id: Int, triggerName: String, parentId: Option[Int], userId: Option[Int], description: Option[String], created: Option[LocalDateTime])

  case class Note(id: Int, title: String, content: String, userId: Option[Int], created: Option[LocalDateTime], lastUpdated: Option[LocalDateTime])

  case class Tag(id: Int, userId: Option[Int], tagName: String, created: Option[LocalDateTime])

  case class EmotionRecordTag(emotionRecordId: Int, tagId: Int, created: Option[LocalDateTime])

  case class NoteTag(noteId: Int, tagId: Int, created: Option[LocalDateTime])

  case class EmotionRecordWithRelations(emotionRecord: EmotionRecord, subEmotions: List[SubEmotion], triggers: List[Trigger])


  object User {
    implicit val userFormat: Format[User] = Json.format[User]

    val parser: RowParser[User] = {
      int("id") ~
        str("username") ~
        str("email") ~
        str("password") ~
        get[Option[LocalDateTime]]("created") map {
        case id ~ username ~ email ~ password ~ created =>
          User(id, username, email, password, created)
      }
    }
  }

  object Emotion {
    implicit val emotionFormat: Format[Emotion] = Json.format[Emotion]

    val parser: RowParser[Emotion] = {
      str("id") ~
        str("emotion_name") ~
        str("emotion_type") ~
        get[Option[LocalDateTime]]("created") map {
        case id ~ emotionName ~ emotionType ~ created =>
          Emotion(id, emotionName, emotionType, created)
      }
    }
  }

  object SubEmotion {
    implicit val subEmotionFormat: Format[SubEmotion] = Json.format[SubEmotion]

    // update column names to be kebab case
    val parser: RowParser[SubEmotion] = {
      str("id") ~
        str("sub_emotion_name") ~
        str("emotion_id") ~
        get[Option[LocalDateTime]]("created")map {
        case id ~ subEmotionName ~ emotionId ~ created =>
          SubEmotion(id, subEmotionName, emotionId, created)
      }
    }
  }

  object EmotionRecord {
    implicit val emotionRecordFormat: Format[EmotionRecord] = Json.format[EmotionRecord]

    val parser: RowParser[EmotionRecord] = {
      int("id") ~
        str("user_id") ~
        str("emotion_id") ~
        int("intensity") ~
        get[Option[LocalDateTime]]("created") map {
        case id ~ userId ~ emotionId  ~ intensity ~ created =>
          EmotionRecord(id, userId, emotionId, intensity, created)
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
        str("description") ~
        get[Option[LocalDateTime]]("created") map {
        case id ~ triggerName ~ parentId ~ userId ~ description ~ created =>
          Trigger(id, triggerName, Option(parentId), Option(userId), Option(description), created)
      }
    }
  }

  object Note {
    implicit val noteFormat: Format[Note] = Json.format[Note]

    val parser: RowParser[Note] = {
      int("id") ~
        str("title") ~
        str("content") ~
        int("userId") ~
        get[Option[LocalDateTime]]("created")~
        get[Option[LocalDateTime]]("lastUpdated") map {
        case id ~ title ~ content ~ userId ~ created ~ lastUpdated =>
          Note(id, title, content, Some(userId), created, lastUpdated)
      }
    }
  }

  object Tag {
    implicit val tagFormat: Format[Tag] = Json.format[Tag]

    val parser: RowParser[Tag] = {
      int("id") ~
        int("userId") ~
        str("tagName") ~
        get[Option[LocalDateTime]]("created") map {
        case id ~ userId ~ tagName ~ created =>
          Tag(id, Some(userId), tagName, created)
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
