package service.ai

import dao.AiAssistant
import play.api.libs.json.{Format, Json}
import service.model.{AiThread, Tool}

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

    def  toAiThread(userId: Long, threadType: String): AiThread =
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
}
