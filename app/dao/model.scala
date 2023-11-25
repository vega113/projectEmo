package dao

import java.time.{LocalDate, LocalDateTime}
import anorm._
import anorm.SqlParser._
import auth.model.TokenData
import play.api.libs.json.{Format, Json}

import scala.annotation.unused
import scala.language.postfixOps

object model {

  case class User(
                   userId: Option[Long],
                   username: String,
                   password: String,
                   firstName: Option[String],
                   lastName: Option[String],
                   email: String,
                   isPasswordHashed: Option[Boolean],
                   created: Option[LocalDateTime] = None
                 ) {
    def toTokenData: TokenData = TokenData(userId.
      getOrElse(throw new RuntimeException(s"No user Id found, username: $username")),
      username, email, firstName.getOrElse(""), lastName.getOrElse(""))
  }

  case class Emotion(id: Option[String], emotionName: Option[String], emotionType: Option[String], description: Option[String] = None)

  case class SubEmotion(
                         subEmotionId: Option[String],
                         subEmotionName: Option[String],
                         description: Option[String],
                         parentEmotionId: Option[String]
                       )

  case class EmotionRecord(
                            id: Option[Long],
                            emotionType: String,
                            userId: Option[Long],
                            emotion: Option[Emotion],
                            intensity: Int,
                            subEmotions: List[SubEmotion],
                            triggers: List[Trigger],
                            notes: List[Note],
                            tags: List[Tag],
                            created: Option[LocalDateTime] = None
                          )

  case class EmotionRecordDay(
                               date: LocalDate,
                               records: List[EmotionRecord]
                             )


  case class Trigger(
                      triggerId: Option[Long],
                      triggerName: Option[String],
                      parentId: Option[Long],
                      createdByUser: Option[Long],
                      description: Option[String],
                      created: Option[LocalDateTime] = None
                    )

  case class Note(
                   id: Option[Long],
                   title: Option[String],
                   text: String,
                   description: Option[String] = None,
                   suggestion: Option[String] = None,
                   todos: Option[List[NoteTodo]] = None,
                   isDeleted: Option[Boolean] = None,
                   lastUpdated: Option[LocalDateTime] = None,
                   lastDeleted: Option[LocalDateTime] = None,
                   created: Option[LocalDateTime] = None
                 )

  case class NoteTodoUpdate(
                             id: Long,
                             isAccepted: Boolean
                           )


  import java.time.LocalDateTime

  case class UserTodo(
                   id: Option[Long],
                   userId: Option[Long],
                   title: String,
                   description: Option[String],
                   color: Option[String],
                   isDone: Boolean,
                   isArchived: Boolean,
                   isDeleted: Boolean,
                   isAi: Boolean,
                   isRead: Boolean,
                   lastUpdated: Option[LocalDateTime] = None,
                   created: Option[LocalDateTime]
                 )

  case class NoteTodo(
                       id: Option[Long],
                       title: String,
                       description: String,
                       isAccepted: Boolean,
                       isAi: Boolean,
                       created: Option[LocalDateTime] = None
                     )

  case class Tag(
                  tagId: Option[Int],
                  tagName: String,
                  created: Option[LocalDateTime] = None
                )

  @unused
  case class EmotionRecordTag(emotionRecordId: Long, tagId: Long)
  @unused
  case class NoteTag(noteId: Long, tagId: Long)

  case class SuggestedAction(
                              id: Option[String],
                              name: String,
                              created: Option[LocalDateTime] = None
                            )

  case class NoteTemplate(
                           id: Option[String],
                           label: String,
                           value: String,
                           created: Option[LocalDateTime] = None
                         )

  case class SunburstData(name: String, value: Option[Int], children: List[SunburstData], color: Option[String] = None)

  case class DoughnutChartData(name: String, recordsCount: Int, intensitySum: Int, color: Option[String] = None)
  case class DoughnutEmotionTypesTriggersChartData(emotionTypes: List[DoughnutChartData], triggers: List[DoughnutChartData])

  case class LineChartTrendDataRow(
                                date: LocalDate,
                                emotionTypeAccumulated: Map[String, LineChartData],
                                triggersAccumulated: Map[String, LineChartData],
                              )

  case class LineChartTrendDataSet(
                                    rows: List[LineChartTrendDataRow],
                                    emotionTypes: List[String],
                                    triggerTypes: List[String],
                                    colors: Map[String, String],
                                  )
  case class LineChartData(recordsCount: Int, intensitySum: Int)





  case class EmotionDetectionResult(
                                     emotionType: String,
                                     intensity: Int,
                                     mainEmotionId: Option[String],
                                     subEmotionId: Option[String],
                                     triggers: Option[List[Trigger]],
                                     tags: Option[List[Tag]],
                                     description: String,
                                     suggestion: String
                                   )

  case class EmotionFromNoteResult(
                                    emotionDetection: EmotionDetectionResult,
                                    note: Note
                                  )


  case class AiDbObj(id: Option[String], response: String, userId: Long, created: Option[LocalDateTime])


  object User {
    implicit val userFormat: Format[User] = Json.format[User]

    implicit val parser: RowParser[User] = {
      get[Option[Long]]("user_id") ~
        str("username") ~
        str("password") ~
        get[Option[String]]("first_name") ~
        get[Option[String]]("last_name") ~
        str("email") ~
        get[Option[Boolean]]("is_password_hashed") ~
        get[Option[LocalDateTime]]("created") map {
        case userId ~ username ~ password ~ firstName ~ lastName ~ email ~ isPasswordHashed ~ created =>
          User(userId, username, password, firstName, lastName, email, isPasswordHashed, created)
      }
    }
  }

  object Emotion {
    implicit val emotionFormat: Format[Emotion] = Json.format[Emotion]

    implicit val parser: RowParser[Emotion] = {
      get[Option[String]]("emotion_id") ~
        get[Option[String]]("emotion_name") ~
        get[Option[String]]("emotion_type") ~
        get[Option[String]]("emotion_description") map {
        case id ~ emotionName ~  emotionType ~ emotionDescription  =>
          Emotion(id, emotionName, emotionType, emotionDescription)
      }
    }
  }

  object SubEmotion {
    implicit val subEmotionFormat: Format[SubEmotion] = Json.format[SubEmotion]

    implicit val parser: RowParser[SubEmotion] = {
      get[Option[String]]("sub_emotion_id") ~
        get[Option[String]]("sub_emotion_name") ~
        get[Option[String]]("sub_emotion_description") ~
        get[Option[String]]("parent_emotion_id") map {
        case subEmotionId ~ subEmotionName ~ description ~ parentEmotionId =>
          SubEmotion(subEmotionId, subEmotionName, description, parentEmotionId)
      }
    }
  }

object EmotionRecord {
  implicit val emotionRecordFormat: Format[EmotionRecord] = Json.format[EmotionRecord]

implicit val parser: RowParser[EmotionRecord] = {
  get[Option[Long]]("id") ~
    str("emotion_type") ~
    get[Option[Long]]("user_id") ~
    get[Option[String]]("emotion_id") ~
    int("intensity") ~
    get[Option[LocalDateTime]]("created") map {
    case id ~ emotionType ~ userId ~ emotionIdOpt  ~ intensity  ~ created =>
      EmotionRecord(id, emotionType, userId,
        emotionIdOpt.map(emotionId => Emotion(Some(emotionId), None, None, None)),
        intensity, List.empty[SubEmotion], List.empty[Trigger], List.empty[Note], List.empty[Tag], created)
  }
}
}

  object Trigger {
    implicit val triggerFormat: Format[Trigger] = Json.format[Trigger]

    implicit val parser: RowParser[Trigger] = {
      get[Option[Long]]("trigger_id") ~
        get[Option[String]]("trigger_name") ~
        get[Option[Long]]("trigger_parent_id") ~
        get[Option[Long]]("created_by_user") ~
        get[Option[String]]("description") ~
        get[Option[LocalDateTime]]("created")map {
        case triggerId ~ triggerName ~ parentId ~ createdByUser ~ description ~ created =>
          Trigger(triggerId, triggerName, parentId, createdByUser, description, created)
      }
    }
  }

  @unused
  object Tag {
    implicit val tagFormat: Format[Tag] = Json.format[Tag]

    implicit val parser: RowParser[Tag] = {
      get[Option[Int]]("id") ~
        str("name") ~
        get[Option[LocalDateTime]]("created") map {
        case tagId ~ tagName ~ created =>
          Tag(tagId, tagName, created)
      }
    }
  }

object Note {
    implicit val noteFormat: Format[Note] = Json.format[Note]

    implicit val parser: RowParser[Note] = {
      get[Option[Long]]("id") ~
        get[Option[String]]("title") ~
        str("text") ~
        get[Option[String]]("description") ~
        get[Option[String]]("suggestion") ~
        get[Option[Boolean]]("is_deleted") ~
        get[Option[LocalDateTime]]("created") map {
        case id ~ title ~ noteText ~ description ~ suggestion ~ isDeleted ~ created =>
          Note(id, title, noteText, description, suggestion, None, isDeleted, created)
      }
    }
  }

  object UserTodo {
    implicit val todoFormat: Format[UserTodo] = Json.format[UserTodo]

    implicit val parser: RowParser[UserTodo] = {
      get[Option[Long]]("id") ~
      get[Option[Long]]("user_id") ~
        str("title") ~
        get[Option[String]]("description") ~
        get[Option[String]]("color") ~
        bool("is_done") ~
        bool("is_archived") ~
        bool("is_deleted") ~
        bool("is_ai") ~
        bool("is_read") ~
        get[Option[LocalDateTime]]("created") ~
        get[Option[LocalDateTime]]("last_updated") map {
        case id ~ userId ~ title ~ description ~ color ~ isDone ~ isArchived ~ isDeleted ~ isAi ~ isRead ~ created
          ~ lastUpdated =>
          UserTodo(id, userId, title, description, color, isDone, isArchived, isDeleted, isAi, isRead, lastUpdated,
            created)
      }
    }
  }

  object NoteTodo {
    implicit val todoFormat: Format[NoteTodo] = Json.format[NoteTodo]

    implicit val parser: RowParser[NoteTodo] = {
      get[Option[Long]]("id") ~
        str("title") ~
        str("description") ~
        bool("is_accepted") ~
        bool("is_ai") ~
        get[Option[LocalDateTime]]("created") map {
        case id ~ title ~ description ~ isAccepted ~ isAi ~ created =>
          NoteTodo(id, title, description, isAccepted, isAi, created)
      }
    }
  }

  object SuggestedAction {
    implicit val suggestedActionFormat: Format[SuggestedAction] = Json.format[SuggestedAction]

    implicit val parser: RowParser[SuggestedAction] = {
      get[Option[String]]("suggested_action_id") ~
        str("suggested_action_name") ~
        get[Option[LocalDateTime]]("created") map {
        case id ~ name ~ created =>
          SuggestedAction(id, name, created)
      }
    }
  }

  object EmotionRecordDay {
    implicit val emotionRecordDayFormat: Format[EmotionRecordDay] = Json.format[EmotionRecordDay]
  }

  object NoteTemplate {
    implicit val noteTemplateFormat: Format[NoteTemplate] = Json.format[NoteTemplate]
    implicit val parser: RowParser[NoteTemplate] = {
      get[Option[String]]("id") ~
        str("label") ~
        str("value") ~
        get[Option[LocalDateTime]]("created") map {
        case id ~ label ~ value ~ created =>
          NoteTemplate(id, label, value, created)
      }
    }
  }

  object EmotionDetectionResult {
    implicit val emotionDetectionResultFormat: Format[EmotionDetectionResult] = Json.format[EmotionDetectionResult]
  }

  object EmotionFromNoteResult {
    implicit val emotionFromNoteResultFormat: Format[EmotionFromNoteResult] = Json.format[EmotionFromNoteResult]
  }

  object SunburstData {
    implicit val sunburstDataFormat: Format[SunburstData] = Json.format[SunburstData]
  }

  object DoughnutChartData {
    implicit val doughnutChartDataFormat: Format[DoughnutChartData] = Json.format[DoughnutChartData]
  }

  object DoughnutEmotionTypesTriggersChartData {
    implicit val doughnutEmotionTypesTriggersChartDataFormat: Format[DoughnutEmotionTypesTriggersChartData] = Json.format[DoughnutEmotionTypesTriggersChartData]
  }

  object LineChartTrendDataRow {
    implicit val lineChartTrendDataFormat: Format[LineChartTrendDataRow] = Json.format[LineChartTrendDataRow]
  }

  object LineChartData {
    implicit val lineChartDataFormat: Format[LineChartData] = Json.format[LineChartData]
  }

  object LineChartTrendDataSet {
    implicit val lineChartTrendDataSetFormat: Format[LineChartTrendDataSet] = Json.format[LineChartTrendDataSet]
  }

  object NoteTodoUpdate {
    implicit val noteTodoUpdateFormat: Format[NoteTodoUpdate] = Json.format[NoteTodoUpdate]
  }

  object ChatGptApiResponse {
    implicit val chatGptApiResponseFormat: Format[AiDbObj] = Json.format[AiDbObj]
    implicit val parser: RowParser[AiDbObj] = {
      get[Option[String]]("id") ~
        str("response") ~
        get[Long]("user_id") ~
        get[Option[LocalDateTime]]("created") map {
        case id ~ response ~ userId ~ created =>
          AiDbObj(id, response, userId, created)
      }
    }
  }
}