package service.ai

import com.google.inject.ImplementedBy
import dao.AiAssistant
import play.api.Configuration
import play.api.libs.json.Json
import service.ai.ChatGptModel._
import service.model.EmoCreateAssistantRequest

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.language.postfixOps

@ImplementedBy(classOf[AiAdminServiceImpl])
trait AiAdminService {
  def createAssistant(emoRequest: EmoCreateAssistantRequest): Future[AiAssistant]

  def modifyAssistant(request: ChatGptCreateAssistantRequest): Future[AiAssistant]

  def deleteAssistantByExternalId(externalId: String): Future[ChatGptDeleteAssistantResponse]
}

class AiAdminServiceImpl @Inject() (aiService: AiDbService, config: Configuration,
                                     aiAssistantApiService: AiAssistantApiService) extends AiAdminService {
  private lazy val logger = play.api.Logger(getClass)
  override def createAssistant(emoRequest: EmoCreateAssistantRequest): Future[AiAssistant] = {
    logger.info(s"Creating assistant for request: $emoRequest")
    val model = config.get[String]("openai.model")
    val urlStr = s"/v1/assistants"
    val gptRequest: ChatGptCreateAssistantRequest = ChatGptCreateAssistantRequest(
      instructions = emoRequest.instructions,
      name = emoRequest.name,
      tools = None,
      model = model,
      fileIds = None,
      metadata = None
    )
    import ChatGptCreateAssistantResponse.createAssistantResponseFormat
    val response: Future[ChatGptCreateAssistantResponse] = aiAssistantApiService.makeApiPostCall(urlStr, gptRequest)
    response.onComplete{
      case scala.util.Success(value) =>
        logger.info(s"Successfully created assistant: $value")
        aiService.saveAiResponseAsync(1, Json.toJson(value))
      case scala.util.Failure(exception) => logger.error(s"Failed to create assistant: $exception")
    }
    val aiAssistant: Future[AiAssistant] = response.map(_.toAiAssistant).map(_.copy(isDefault = emoRequest.isDefault,
      assistantType = Option(emoRequest.assistantType)))
    aiAssistant.onComplete {
      case scala.util.Success(value) =>
        aiService.saveAiAssistantAsync(value)
        logger.info(s"Successfully saved assistant: $value")
      case scala.util.Failure(exception) => logger.error(s"Failed to save assistant: $exception")
    }
    aiAssistant
  }

  override def modifyAssistant(request: ChatGptCreateAssistantRequest): Future[AiAssistant] = ???

  override def deleteAssistantByExternalId(externalId: String): Future[ChatGptDeleteAssistantResponse] = {
    logger.info(s"Deleting assistant with external id: $externalId")

    val path = s"/v1/assistants/$externalId"

    import ChatGptDeleteAssistantResponse.deleteAssistantResponseFormat
    val response = aiAssistantApiService.makeApiDeleteCall(path)
    response.onComplete {
      case scala.util.Success(value) =>
        logger.info(s"Successfully deleted assistant: $value")
        aiService.deleteAiAssistantByExternalId(externalId)
        aiService.saveAiResponseAsync(1, Json.toJson(value))
      case scala.util.Failure(exception) => logger.error(s"Failed to delete assistant: $exception")
    }
    response
  }
}
