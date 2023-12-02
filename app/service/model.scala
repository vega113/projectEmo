package service

import dao.model.{NoteTodo, Tag, Trigger}
import play.api.libs.json.{Format, Json}

import java.time.{LocalDateTime, ZoneOffset}

object model {
  case class DetectEmotionRequest(text: String, userId: Long)

  case class ChatGptMetadata(value1: String)
  object ChatGptMetadata {
    implicit val chatGptMetadataFormat: Format[ChatGptMetadata] = Json.format[ChatGptMetadata]
  }
  case class ChatGptCreateAssistantRequest(instructions: String,
                                           name: String,
                                           tools: Option[List[(String, String)]],
                                           model: String,
                                           fileIds: Option[List[String]], // up to 20 files
                                           metadata: Option[ChatGptMetadata] // up to 16 key-value pairs. Keys can be a maximum of 64 characters long and values can be a maxium of 512 characters long.
                                   )
  case class ChatGptCreateAssistantResponse(
                                      id: String,
                                      `object`: String,
                                      created_at: Long,
                                      name: String,
                                      description: Option[String],
                                      model: String,
                                      instructions: String,
                                      tools: List[Tool],
                                      file_ids: List[String],
                                      metadata: Map[String, String]
                                    ) {
    def toAiAssistant: AiAssistant = {
      AiAssistant(
        id = None,
        externalId = id,
        name = name,
        description = description,
        isDefault = false,
        created = LocalDateTime.now(),
        lastUpdated = Option(LocalDateTime.now()),
        createdAtProvider = created_at,
        assistantType = Some("EmoDetection")
      )
    }
  }

  case class EmoCreateAssistantRequest(
                                        instructions: String,
                                        name: String,
                                        isDefault: Boolean
                                      )

  object EmoCreateAssistantRequest {
    implicit val emoCreateAssistantRequestFormat: Format[EmoCreateAssistantRequest] = Json.format[EmoCreateAssistantRequest]
  }

  case class ChatGptDeleteAssistantResponse(
                                              id: String,
                                              `object`: String,
                                              deleted: Boolean
                                           )
  object ChatGptDeleteAssistantResponse {
    implicit val deleteAssistantResponseFormat: Format[ChatGptDeleteAssistantResponse] = Json.format[ChatGptDeleteAssistantResponse]
  }

  case class AiAssistant(
                          id: Option[Int],
                          externalId: String,
                          name: String,
                          description: Option[String],
                          isDefault: Boolean,
                          created: LocalDateTime,
                          lastUpdated: Option[LocalDateTime],
                          createdAtProvider: Long,
                          assistantType: Option[String]
                        )

  case class AiThread(
                       id: Int,
                       externalId: String,
                       userId: Long,
                       threadType: String,
                       isDeleted: Boolean,
                       created: LocalDateTime,
                     )

  case class AiMessage(
                        id: Int,
                        externalId: String,
                        threadId: Long,
                        externalThreadId: String,
                        userId: Long,
                        role: String,
                        message: String,
                        externalCreated: Long,
                        created: LocalDateTime,
                        rawResponse: String
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

  object ChatGptCreateAssistantRequest {
    implicit val createAssistantRequestFormat: Format[ChatGptCreateAssistantRequest] = Json.format[ChatGptCreateAssistantRequest]
  }

  object AiAssistant {
    implicit val aiAssistantFormat: Format[AiAssistant] = Json.format[AiAssistant]
  }

  object AiThread {
    implicit val aiThreadFormat: Format[AiThread] = Json.format[AiThread]
  }

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

  object ChatGptCreateAssistantResponse {
    implicit val createAssistantResponseFormat: Format[ChatGptCreateAssistantResponse] = Json.format[ChatGptCreateAssistantResponse]
  }
}
