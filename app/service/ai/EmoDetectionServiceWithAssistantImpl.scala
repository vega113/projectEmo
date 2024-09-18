package service.ai

import dao.model.EmotionDetectionResult
import io.cequence.openaiscala.domain.RunStatus
import play.api.libs.json.Json
import service.model.DetectEmotionRequest

import javax.inject.{Inject, Named}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Named("ChatGptAssistant")
class EmoDetectionServiceWithAssistantImpl @Inject()(
                                                      aiAssistantService: AiAssistantService,
                                                    ) extends EmotionDetectionService {
  private val assistantType = "EmoDetection"

  private lazy val logger = play.api.Logger(getClass)

  override def detectEmotion(request: DetectEmotionRequest): Future[EmotionDetectionResult] = {
    logger.info(s"V2 Detecting emotion for request: $request")
    val responseFuture: Future[EmotionDetectionResult] = for {
      aiAssistantId <- aiAssistantService.fetchAssistantForUser(request.userId, assistantType)
      externalThreadId <- aiAssistantService.createOrFetchThreadId(request.userId, aiAssistantId, assistantType)
      _ <- aiAssistantService.addMessageToThread(externalThreadId, request.text)
      threadRunInitial <- aiAssistantService.runThread(externalThreadId, aiAssistantId, None)
      runWithFunctionCall <- aiAssistantService.pollThreadRunUntilNeededStatus(externalThreadId, threadRunInitial.id,
        RunStatus.RequiresAction)
      emoDetectionResult = {
        val funCallResult = runWithFunctionCall.required_action.get.submit_tool_outputs.tool_calls.find(
            _.function.name == "detect_emotion").getOrElse(
        throw new RuntimeException("Failed to find detect_emotion tool call")
        )
        logger.info(s"funCallResult: $funCallResult")
        funCallResult
      }
      _ <- aiAssistantService.submitToolOutput(runWithFunctionCall)
    } yield {
      parseAiResponse(emoDetectionResult.function.arguments)
    }
    responseFuture.onComplete {
      case scala.util.Success(_) =>
        logger.info(s"V2 Successfully detected emotion for request, userId: ${request.userId}")
      case scala.util.Failure(e) =>
        logger.error(s"V2 Failed to detect emotion for request, userId: ${request.userId}", e)
    }
    responseFuture
  }

  private def parseAiResponse(message: String) = {
    Json.parse(message).as[EmotionDetectionResult]
  }
}
