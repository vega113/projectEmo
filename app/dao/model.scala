package dao

import java.time.LocalDateTime

import anorm._
import anorm.SqlParser._
import play.api.libs.json.{Format, Json}
import scala.language.postfixOps

object model {

  case class User(
                   userId: Option[Int],
                   username: String,
                   password: String,
                   firstName: Option[String],
                   lastName: Option[String],
                   email: String,
                   isPasswordHashed: Boolean,
                   created: LocalDateTime
                 )

  case class Emotion(id: String, emotionName: String, emotionType: String)

  case class SubEmotion(
                         subEmotionId: Option[String],
                         subEmotionName: String,
                         parentEmotionId: Option[String]
                       )

  case class EmotionRecord(
                            id: Option[Int],
                            userId: Int,
                            emotionId: String,
                            intensity: Int,
                            created: LocalDateTime
                          )

  case class Trigger(
                      triggerId: Option[Int],
                      triggerName: String,
                      parentId: Option[Int],
                      createdByUser: Option[Int],
                      description: Option[String],
                      created: LocalDateTime
                    )

  case class Note(
                   noteId: Option[Int],
                   title: Option[String],
                   noteText: String,
                   noteUserId: Int,
                   created: LocalDateTime
                 )

  case class Tag(
                  tagId: Option[Int],
                  tagName: String,
                  created: LocalDateTime
                )

  case class EmotionRecordTag(emotionRecordId: Int, tagId: Int)

  case class NoteTag(noteId: Int, tagId: Int)

  case class EmotionRecordWithRelations(
                                         emotionRecord: EmotionRecord,
                                         subEmotions: List[SubEmotion]
                                       )

  object User {
    implicit val userFormat: Format[User] = Json.format[User]

    implicit val parser: RowParser[User] = {
      get[Option[Int]]("user_id") ~
        str("username") ~
        str("password") ~
        get[Option[String]]("first_name") ~
        get[Option[String]]("last_name") ~
        str("email") ~
        bool("is_password_hashed") ~
        get[LocalDateTime]("created") map {
        case userId ~ username ~ password ~ firstName ~ lastName ~ email ~ isPasswordHashed ~ created =>
          User(userId, username, password, firstName, lastName, email, isPasswordHashed, created)
      }
    }
  }

  object Emotion {
    implicit val emotionFormat: Format[Emotion] = Json.format[Emotion]

    implicit val parser: RowParser[Emotion] = {
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

    implicit val parser: RowParser[SubEmotion] = {
      get[Option[String]]("sub_emotion_id") ~
        str("sub_emotion_name") ~
        get[Option[String]]("parent_emotion_id") map {
        case subEmotionId ~ subEmotionName ~ parentEmotionId =>
          SubEmotion(subEmotionId, subEmotionName, parentEmotionId)
      }
    }
  }

  object EmotionRecord {
    implicit val emotionRecordFormat: Format[EmotionRecord] = Json.format[EmotionRecord]

    implicit val parser: RowParser[EmotionRecord] = {
      get[Option[Int]]("id") ~
        int("user_id") ~
        str("emotion_id") ~
        int("intensity") ~
        get[LocalDateTime]("created") map {
        case id ~ userId ~ emotionId ~ intensity ~ created =>
          EmotionRecord(id, userId, emotionId, intensity, created)
      }
    }

  }

  object Trigger {
    implicit val triggerFormat: Format[Trigger] = Json.format[Trigger]

    implicit val parser: RowParser[Trigger] = {
      get[Option[Int]]("trigger_id") ~
        str("trigger_name") ~
        get[Option[Int]]("parent_id") ~
        get[Option[Int]]("created_by_user") ~
        get[Option[String]]("description") ~
        get[LocalDateTime]("created") map {
        case triggerId ~ triggerName ~ parentId ~ createdByUser ~ description ~ created =>
          Trigger(triggerId, triggerName, parentId, createdByUser, description, created)
      }
    }
  }

  object Tag {
    implicit val tagFormat: Format[Tag] = Json.format[Tag]

    implicit val parser: RowParser[Tag] = {
      get[Option[Int]]("tag_id") ~
        str("tag_name") ~
        get[LocalDateTime]("created") map {
        case tagId ~ tagName ~ created =>
          Tag(tagId, tagName, created)
      }
    }
  }

object Note {
    implicit val noteFormat: Format[Note] = Json.format[Note]

    implicit val parser: RowParser[Note] = {
      get[Option[Int]]("note_id") ~
        get[Option[String]]("title") ~
        str("note_text") ~
        int("note_user_id") ~
        get[LocalDateTime]("created") map {
        case noteId ~ title ~ noteText ~ noteUserId ~ created =>
          Note(noteId, title, noteText, noteUserId, created)
      }
    }
  }

  object EmotionRecordWithRelations {
    implicit val emotionRecordWithRelationsFormat: Format[EmotionRecordWithRelations] = Json.format[EmotionRecordWithRelations]

    val parser: RowParser[EmotionRecordWithRelations] = {
      EmotionRecord.parser ~
        get[Option[String]]("sub_emotion_id") ~
        str("sub_emotion_name") ~
        get[Option[String]]("parent_emotion_id") map {
        case emotionRecord ~ subEmotionId ~ subEmotionName ~ parentEmotionId =>
          val subEmotion = SubEmotion(subEmotionId, subEmotionName, parentEmotionId)
          EmotionRecordWithRelations(emotionRecord, List(subEmotion))
      }
    }
  }
}