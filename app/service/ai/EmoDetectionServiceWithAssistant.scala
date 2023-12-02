package service.ai

import com.google.inject.ImplementedBy
import dao.model.EmotionDetectionResult
import play.api.libs.json.Json
import service.model.{DetectEmotionRequest, EmotionDetectionEmotions, EmotionDetectionExtendedResult}

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
    logger.info(s"Detecting emotion for request: $request")
    // fetch assistant for user
    // fetch thread for user
    // add new message to thread
    // run assistant
    // fetch last message by assistant for thread older than date externalCreatedAt
    for {
      aiAssistant <- aiAssistantService.fetchAssistantForUser(request.userId, assistantType)
      aiThread <- aiAssistantService.fetchThreadForUser(request.userId)
      externalThreadId <- aiAssistantService.createOrFetchThread(request.userId, aiAssistant, assistantType).map(_.externalId)
      aiMessage <- aiAssistantService.addMessageToThread(externalThreadId, request.text)
      instructions <- aiAssistantService.makeRunInstructionsForUser(request.userId)
      threadRunInitial <- aiAssistantService.runAssistant(externalThreadId, aiAssistant.externalId, instructions)
      threadRunCompleted <- aiAssistantService.pollThreadRunUntilComplete(externalThreadId, threadRunInitial.id)
      responseAiMessage <- aiAssistantService.fetchLastMessageByAssistantForThreadOlderThan(aiMessage)
    } yield {
      logger.info(s"Response from AI: $responseAiMessage")
      parseAiResponse(responseAiMessage.message)
    }
  }

  private def parseAiResponse(message: String) = {
    Json.parse(message).as[EmotionDetectionResult]
  }
}
