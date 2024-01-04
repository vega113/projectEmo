package service.ai

import dao.model.EmotionDetectionResult
import play.api.{Configuration, Logger}
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.libs.ws.WSClient
import service.model.DetectEmotionRequest
import service.serviceModel.ChatGptApiResponse

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.{Success, Try}

@Named("ChatGpt")
class ChatGptEmotionDetectionServiceImpl @Inject()(ws: WSClient, config: Configuration, aiService: AiDbService)(implicit ec: ExecutionContext) extends EmotionDetectionService {

  private final val logger: Logger = play.api.Logger(getClass)
  private final val fakeEmoDetectionResult = "{\"emotionType\":\"Positive\",\"intensity\":3,\"mainEmotionId\":\"Joy\",\"subEmotionId\":\"Serenity\",\"description\":\"Listening to Dada Istamaya's spiritual experience and feeling the inner silence, love, and beauty inspires you and brings you joy.\",\"suggestion\":\"Take a moment to reflect on the emotions and sensations you felt during the video. Explore ways to incorporate more moments of inner silence, love, and beauty into your own life, such as through meditation or engaging in activities that bring you joy and inspiration.\",\"triggers\":[{\"triggerName\":\"Other\"},{\"triggerName\":\"Spiritual experience\"}],\"tags\":[{\"tagName\":\"joy\"},{\"tagName\":\"inspiration\"},{\"tagName\":\"inner silence\"},{\"tagName\":\"love\"},{\"tagName\":\"beauty\"}]}"

  override def detectEmotion(request: DetectEmotionRequest): Future[EmotionDetectionResult] = {
    val startTime = System.nanoTime()
    val responseFuture =  if(request.text.startsWith("FAKE")) {
      Future.successful(Json.parse(fakeEmoDetectionResult).as[EmotionDetectionResult])
    } else {
      makeApiCall(request).recoverWith {
        case e: Exception =>
          logger.error(s"V1 Failed to detect emotion for request: $request", e)
          Future.failed(e)
      }
    }
    responseFuture.onComplete {
      case scala.util.Success(_) =>
        logger.info(s"V1 Successfully detected emotion for request, userId: ${request.userId}")
      case scala.util.Failure(e) =>
        logger.error(s"V1 Failed to detect emotion for request, userId: ${request.userId}", e)
    }
    responseFuture.andThen {
      case Success(x) =>
        val endTime = System.nanoTime()
        val elapsedTime = (endTime - startTime) / 1e9d
        aiService.saveAiResponse(request.userId, EmotionDetectionResult.emotionDetectionResultFormat.writes(x),
          Option(request.text),
          Option("emo detection v1"), Option(elapsedTime))
        logger.info(s"Total elapsed time for detectEmotion V1: $elapsedTime seconds")
    }
  }

  /**
   * Makes an API call to detect emotion based on the given request.
   */
  private def makeApiCall(request: DetectEmotionRequest): Future[EmotionDetectionResult] = {
    val headers = createHeaders

    val payload = createPayload(request)
    val timeoutDuration = config.get[Duration]("openai.timeout")
    logger.info(s"Making API call with payload: $payload, timeout: $timeoutDuration")

    val url =  config.get[String]("openai.baseUrl") + "/v1/chat/completions"
    // Make the API call
    ws.url(url)
      .withRequestTimeout(timeoutDuration)
      .withHttpHeaders(headers.toSeq: _*)
      .post(payload)
      .flatMap { response =>
        if (response.status == 200) {
          Future.fromTry {
            Try {
              response.json.validate[ChatGptApiResponse] match {
                case JsSuccess(result, _) =>
                  logger.info(s"Deserialization successful: $result")
                  val content = result.choices.head.message.content
                  Json.parse(content).as[EmotionDetectionResult]
                case JsError(errors) =>
                  logger.error(s"Deserialization failed: $errors, response: ${response.json}")
                  throw new Exception(s"Deserialization failed: $errors")
              }
            }
          }
        } else {
          logger.error(s"Received unexpected status ${response.status} : ${response.body}")
          Future.failed(new Exception(s"Received unexpected status ${response.status} : ${response.body}"))
        }
      }
  }

  private def createPayload(request: DetectEmotionRequest) = {
    Json.obj(
      "model" -> config.get[String]("openai.model"),
      "messages" -> Json.arr(
        Json.obj(
          "role" -> "system",
          "content" -> config.get[String]("openai.systemPromt")
        ),
        Json.obj(
          "role" -> "user",
          "content" -> request.text
        )
      ),
      "max_tokens" -> 2000,
      "temperature" -> 0.99
    )
  }

  private def createHeaders = {
    Map(
      "Content-Type" -> "application/json",
      "Authorization" -> s"Bearer ${config.get[String]("openai.apikey")}"
    )
  }
}
