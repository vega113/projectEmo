package service.ai

import dao.AiAssistant
import play.api.libs.json.{Format, Json}
import service.ai.ChatGptModel.{ChatGptMessageResponse, Content, TextContent}
import service.model.{AiMessage, AiThread, Tool}

import java.time.LocalDateTime

object ChatGptModel {

  case class ChatGptCreateAssistantRequest(instructions: String,
                                           name: String,
                                           tools: Option[List[(String, String)]],
                                           model: String,
                                           fileIds: Option[List[String]], // up to 20 files
                                           metadata: Option[Map[String, String]] // up to 16 key-value pairs. Keys can be a maximum of 64 characters long and values can be a maxium of 512 characters long.
                                          )

  object ChatGptCreateAssistantRequest {
    implicit val createAssistantRequestFormat: Format[ChatGptCreateAssistantRequest] = Json.format[ChatGptCreateAssistantRequest]
  }

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

  object ChatGptCreateAssistantResponse {
    implicit val createAssistantResponseFormat: Format[ChatGptCreateAssistantResponse] = Json.format[ChatGptCreateAssistantResponse]
  }

  case class ChatGptDeleteAssistantResponse(
                                             id: String,
                                             `object`: String,
                                             deleted: Boolean
                                           )

  object ChatGptDeleteAssistantResponse {
    implicit val deleteAssistantResponseFormat: Format[ChatGptDeleteAssistantResponse] = Json.format[ChatGptDeleteAssistantResponse]
  }

  case class ChatGptCreateThreadResponse(
                                          id: String,
                                          `object`: String,
                                          created_at: Long,
                                          metadata: Map[String, String]
                                        ) {

    def toAiThread(userId: Long, threadType: String): AiThread =
      AiThread(
        id = None,
        externalId = id,
        userId = userId,
        threadType = threadType,
        isDeleted = false,
        created = None
      )
  }

  object ChatGptCreateThreadResponse {
    implicit val createThreadResponseFormat: Format[ChatGptCreateThreadResponse] = Json.format[ChatGptCreateThreadResponse]
  }

  case class TextContent(value: String, annotations: List[Map[String, String]])

  case class Content(`type`: String, text: TextContent)

  case class ChatGptMessageResponse(id: String, `object`: String, created_at: Long, thread_id: String, role: String,
                                    content: List[Content], file_ids: List[String],
                                    assistant_id: Option[String], run_id: Option[String],
                                    metadata: Map[String, String]) {
    def toAiMessage: AiMessage = {
      val message = content.map(_.text.value).mkString(" ")
      AiMessage(
        externalId = id,
        externalThreadId = thread_id,
        role = role,
        message = message,
        externalCreated = created_at,
        created = LocalDateTime.now()
      )
    }
  }


  object TextContent {
    implicit val textContentFormat: Format[TextContent] = Json.format[TextContent]
  }

  object Content {
    implicit val contentFormat: Format[Content] = Json.format[Content]
  }

  object ChatGptMessageResponse {
    implicit val addMessageRequestFormat: Format[ChatGptMessageResponse] = Json.format[ChatGptMessageResponse]
  }

  case class ChatGptAddMessageRequest(
                                       role: String,
                                       content: String,
                                       file_ids: Option[List[String]],
                                       metadata: Option[Map[String, String]]
                                     )

  object ChatGptAddMessageRequest {
    implicit val addMessageRequestFormat: Format[ChatGptAddMessageRequest] = Json.format[ChatGptAddMessageRequest]
  }

  case class ChatGptThreadRunRequest(
                                         assistant_id: String,
                                         instructions: Option[String],
                                         model: Option[String],
                                       )
  object ChatGptThreadRunRequest {
    implicit val runAssistantRequestFormat: Format[ChatGptThreadRunRequest] = Json.format[ChatGptThreadRunRequest]
  }

  case class ChatGptThreadRunResponse(
                                          id: String,
                                          `object`: String,
                                          created_at: Long,
                                          assistant_id: String,
                                          thread_id: String,
                                          status: String,
                                          started_at: Option[Long],
                                          expires_at: Option[Long],
                                          cancelled_at: Option[Long],
                                          failed_at: Option[Long],
                                          completed_at: Option[Long],
                                          last_error: Option[String],
                                          model: String,
                                          instructions: String,
                                          tools: List[Tool],
                                          file_ids: List[String],
                                          metadata: Map[String, String]
                                        ) {

  }

  object ChatGptThreadRunResponse {
    implicit val runAssistantResponseFormat: Format[ChatGptThreadRunResponse] = Json.format[ChatGptThreadRunResponse]
  }

  case class ChatGptResponseMessages(
                                      `object`: String,
                                      data: List[ChatGptMessageResponse],
                                      first_id: String,
                                      last_id: String,
                                      has_more: Boolean,
                                    ){

  }

  object ChatGptResponseMessages {
    implicit val responseMessagesFormat: Format[ChatGptResponseMessages] = Json.format[ChatGptResponseMessages]
  }
}
