package service.ai

import com.google.inject.ImplementedBy
import dao.model.{EmotionDetectionResult, RequestsInFlight}
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, Json}
import service.RequestsInFlightService
import service.model._

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

trait EmotionDetectionService {
  def detectEmotion(request: DetectEmotionRequest): Future[EmotionDetectionResult]
}

@ImplementedBy(classOf[CompositeEmotionDetectionServiceImpl])
trait EmotionDetectionServiceWithIdempotency {
  def detectEmotion(request: DetectEmotionRequest, idempotencyKey: String): Future[Option[EmotionDetectionResult]]
}


class CompositeEmotionDetectionServiceImpl @Inject()(@Named("ChatGpt") v1: EmotionDetectionService,
                                                     @Named("ChatGptAssistant") v2: EmotionDetectionService,
                                                     requestsInFlightService: RequestsInFlightService,
                                                     aiService: AiDbService
                                                    )(implicit ec: ExecutionContext) extends EmotionDetectionServiceWithIdempotency {
  private final val logger: Logger = play.api.Logger(getClass)

  override def detectEmotion(request: DetectEmotionRequest, idempotencyKey: String): Future[Option[EmotionDetectionResult]] = {
    val startTime = System.nanoTime()

    def saveResponseToDb(emotionFuture: Future[EmotionDetectionResult], responseType: String, idempotencyKey: String) = {
      emotionFuture.andThen {
        case Success(x) =>
          val endTime = System.nanoTime()
          val elapsedTime = (endTime - startTime) / 1e9d
          aiService.saveAiResponse(request.userId, EmotionDetectionResult.emotionDetectionResultFormat.writes(x),
            Option(request.text),
            Option(s"emo detection $responseType"), Option(elapsedTime), Some(idempotencyKey))
          logger.info(s"Total elapsed time for detectEmotion $responseType: $elapsedTime seconds")
          requestsInFlightService.markRequestComplete(idempotencyKey)
      }
    }

    requestsInFlightService.fetchOrCreateRequestInFlight(idempotencyKey).flatMap {
      case Some(requestsInFlight) if requestsInFlight.isCompleted =>
        logger.info(s"Request with idempotencyKey: $idempotencyKey is already completed, fetching from db")
        fetchCompletedEmotionDetectionResult(requestsInFlight).map(Some(_))
      case _ =>
        logger.info(s"Request with idempotencyKey: $idempotencyKey does not exist or is not completed, running emotion detection")
        val v1EmotionFuture: Future[EmotionDetectionResult] = v1.detectEmotion(request)
        saveResponseToDb(v1EmotionFuture, "V1", idempotencyKey)
        Future.successful(None)
    }
  }

  private def fetchCompletedEmotionDetectionResult(requestsInFlight: RequestsInFlight) = {
    aiService.fetchAiResponseByRequestId(requestsInFlight.requestId).map { aiResponse =>
      EmotionDetectionResult.emotionDetectionResultFormat.reads(Json.parse(aiResponse.response)) match {
        case JsSuccess(value, _) => value
        case JsError(errors) =>
          logger.error(s"Failed to parse emotion detection result from db: $errors")
          throw new Exception(s"Failed to parse emotion detection result from db: $errors")
      }
    }
  }
}