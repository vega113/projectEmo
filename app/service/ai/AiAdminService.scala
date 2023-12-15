package service.ai

import com.google.inject.ImplementedBy
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import service.model.{AiAssistant, ChatGptCreateAssistantRequest, ChatGptCreateAssistantResponse, ChatGptDeleteAssistantResponse, EmoCreateAssistantRequest}

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
    val apiKey = config.get[String]("openai.apikey")
    val timeoutDuration = config.get[Duration]("openai.timeout")
    val baseUrl = config.get[String]("openai.baseUrl")
    val model = config.get[String]("openai.model")
    val urlStr = s"$baseUrl/v1/assistants"
    val gptRequest: ChatGptCreateAssistantRequest = ChatGptCreateAssistantRequest(
      instructions = emoRequest.instructions,
      name = emoRequest.name,
      tools = None,
      model = model,
      fileIds = None,
      metadata = None
    )
    import service.model.ChatGptCreateAssistantResponse.createAssistantResponseFormat
    val response: Future[ChatGptCreateAssistantResponse] = aiAssistantApiService.makeApiPostCall(gptRequest, urlStr,
      timeoutDuration, apiKey)
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

    val apiKey = config.get[String]("openai.apikey")
    val timeoutDuration = config.get[Duration]("openai.timeout")
    val baseUrl = config.get[String]("openai.baseUrl")
    val model = config.get[String]("openai.model")
    val urlStr = s"$baseUrl/v1/assistants/$externalId"

    import service.model.ChatGptDeleteAssistantResponse.deleteAssistantResponseFormat
    val response = aiAssistantApiService.makeApiDeleteCall(urlStr, timeoutDuration, apiKey)
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
