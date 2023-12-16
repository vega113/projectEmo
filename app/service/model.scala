package service

import anorm.{Macro, RowParser}
import dao.model.{NoteTodo, Tag, Trigger}
import play.api.libs.json.{Format, Json}
import Macro.ColumnNaming
import dao.AiAssistant

import java.time.LocalDateTime

object model {
  case class DetectEmotionRequest(text: String, userId: Long)



  case class EmoCreateAssistantRequest(
                                        instructions: String,
                                        name: String,
                                        isDefault: Boolean,
                                        assistantType: String = "EmoDetection"
                                      )

  object EmoCreateAssistantRequest {
    implicit val emoCreateAssistantRequestFormat: Format[EmoCreateAssistantRequest] = Json.format[EmoCreateAssistantRequest]
  }


  case class AiThread(
                       id: Option[Long],
                       externalId: String,
                       userId: Long,
                       threadType: String,
                       isDeleted: Boolean,
                       created: Option[LocalDateTime],
                     )
  object AiThread {
    implicit val aiThreadFormat: Format[AiThread] = Json.format[AiThread]
    implicit val parser: RowParser[AiThread] = Macro.namedParser[AiThread](ColumnNaming.SnakeCase)
  }

  case class AiMessage(
                        externalId: String,
                        externalThreadId: String,
                        role: String,
                        message: String,
                        externalCreated: Long,
                        created: LocalDateTime
                      )

  case class EmotionDetectionResultAssistant(
                                              emo: EmotionDetectionEmotions,
                                              extra: EmotionDetectionExtendedResult,
                                            )

  case class EmotionDetectionEmotions(
                                       emotionType: String,
                                       intensity: Int,
                                       mainEmotionId: Option[String],
                                       subEmotionId: Option[String],
                                       triggers: Option[Trigger],
                                     )

  case class EmotionDetectionExtendedResult(
                                             tags: Option[List[Tag]],
                                             todos: Option[List[NoteTodo]],
                                             textTitle: Option[String],
                                             description: String,
                                             suggestion: String,
                                             text: String,
                                             prettyText: String,
                                             blogText: String,
                                           )

  case class Tool(`type`: String)

  case class ThreadRun(
                        id: String,
                        `object`: String,
                        created_at: Long,
                        assistant_id: String,
                        thread_id: String,
                        status: String,
                        started_at: Option[Long],
                        expires_at: Long,
                        cancelled_at: Option[Long],
                        failed_at: Option[Long],
                        completed_at: Option[Long],
                        last_error: Option[String],
                        model: String,
                        instructions: String,
                        tools: List[Tool],
                        file_ids: List[String],
                        metadata: Map[String, String]
                      )

  case class Text(value: String, annotations: List[String])

  case class Content(`type`: String, text: Text)

  case class ThreadMessage(
                            id: String,
                            `object`: String,
                            created_at: Long,
                            thread_id: String,
                            role: String,
                            content: List[Content],
                            file_ids: List[String],
                            assistant_id: String,
                            run_id: String,
                            metadata: Map[String, String]
                          )

  case class AiDataCollection[T](
                               `object`: String,
                               data: List[T],
                               first_id: String,
                               last_id: String,
                               has_more: Boolean,
                             )



  object AiMessage {
    implicit val aiMessageFormat: Format[AiMessage] = Json.format[AiMessage]
  }

  object EmotionDetectionResultAssistant {
    implicit val emotionDetectionResultAssistantFormat: Format[EmotionDetectionResultAssistant] = Json.format[EmotionDetectionResultAssistant]
  }

  object EmotionDetectionEmotions {
    implicit val emotionDetectionEmotionsFormat: Format[EmotionDetectionEmotions] = Json.format[EmotionDetectionEmotions]
  }

  object EmotionDetectionExtendedResult {
    implicit val emotionDetectionExtendedResultFormat: Format[EmotionDetectionExtendedResult] = Json.format[EmotionDetectionExtendedResult]
  }

  object Tool {
    implicit val toolFormat: Format[Tool] = Json.format[Tool]
  }
  object ThreadRun {
    implicit val threadRunFormat: Format[ThreadRun] = Json.format[ThreadRun]
  }

  object Text {
    implicit val textFormat: Format[Text] = Json.format[Text]
  }

  object Content {
    implicit val contentFormat: Format[Content] = Json.format[Content]
  }
  object ThreadMessage {
    implicit val threadMessageFormat: Format[ThreadMessage] = Json.format[ThreadMessage]
  }


}
