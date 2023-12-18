package service.ai

import com.google.inject.ImplementedBy
import dao.model.EmotionDetectionResult
import play.api.libs.json.Json
import service.model.DetectEmotionRequest

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[EmoDetectionServiceWithAssistantImpl])
trait EmoDetectionServiceWithAssistant {
  def detectEmotion(request: DetectEmotionRequest): Future[EmotionDetectionResult]
}

class EmoDetectionServiceWithAssistantImpl @Inject()(
                                                      aiAssistantService: AiAssistantService,
                                                    ) extends EmoDetectionServiceWithAssistant {
  private val assistantType = "EmoDetection"

  private lazy val logger = play.api.Logger(getClass)

  override def detectEmotion(request: DetectEmotionRequest): Future[EmotionDetectionResult] = {
    val startTime = System.nanoTime()
    logger.info(s"V2 Detecting emotion for request: $request")
    val responseFuture = for {
      aiAssistant <- aiAssistantService.fetchAssistantForUser(request.userId, assistantType)
      externalThreadId <- aiAssistantService.createOrFetchThread(request.userId, aiAssistant, assistantType).map(_.externalId)
      aiMessage <- aiAssistantService.addMessageToThread(externalThreadId, request.text)
      instructions <- aiAssistantService.makeRunInstructionsForUser(request.userId)
      threadRunInitial <- aiAssistantService.runThread(externalThreadId, aiAssistant.externalId, instructions)
      _ <- aiAssistantService.pollThreadRunUntilComplete(externalThreadId, threadRunInitial.id)
      responseAiMessage <- aiAssistantService.fetchLastMessageByAssistantForThreadOlderThan(aiMessage)
    } yield {
      parseAiResponse(responseAiMessage.message)
    }
    responseFuture.onComplete {
      case scala.util.Success(_) =>
        logger.info(s"Successfully detected emotion for request using V2, userId: ${request.userId}")
      case scala.util.Failure(e) =>
        logger.error(s"Failed to detect emotion for request using V2, userId: ${request.userId}", e)
    }
    responseFuture.andThen {
      case _ =>
        val endTime = System.nanoTime()
        val elapsedTime = (endTime - startTime) / 1e9d
        logger.info(s"Total elapsed time for detectEmotion V2: $elapsedTime seconds")
    }
    responseFuture
  }

  private def parseAiResponse(message: String) = {
    Json.parse(message).as[EmotionDetectionResult]
  }
}
