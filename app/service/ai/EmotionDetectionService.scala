package service.ai

import com.google.inject.ImplementedBy
import dao.model.EmotionDetectionResult
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import service.model._
import service.serviceModel.ChatGptApiResponse

import javax.inject.Inject
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@ImplementedBy(classOf[ChatGptEmotionDetectionServiceImpl])
trait EmotionDetectionService {
  def detectEmotion(request: DetectEmotionRequest): Future[EmotionDetectionResult]
}

class ChatGptEmotionDetectionServiceImpl @Inject()(ws: WSClient, config: Configuration, aiService: AiDbService)(implicit ec: ExecutionContext) extends EmotionDetectionService {

  private final val logger: Logger = play.api.Logger(getClass)
  private final val fakeEmoDetectionResult = "{\"emotionType\":\"Positive\",\"intensity\":3,\"mainEmotionId\":\"Joy\",\"subEmotionId\":\"Serenity\",\"description\":\"Listening to Dada Istamaya's spiritual experience and feeling the inner silence, love, and beauty inspires you and brings you joy.\",\"suggestion\":\"Take a moment to reflect on the emotions and sensations you felt during the video. Explore ways to incorporate more moments of inner silence, love, and beauty into your own life, such as through meditation or engaging in activities that bring you joy and inspiration.\",\"triggers\":[{\"triggerName\":\"Other\"},{\"triggerName\":\"Spiritual experience\"}],\"tags\":[{\"tagName\":\"joy\"},{\"tagName\":\"inspiration\"},{\"tagName\":\"inner silence\"},{\"tagName\":\"love\"},{\"tagName\":\"beauty\"}]}"

  override def detectEmotion(request: DetectEmotionRequest): Future[EmotionDetectionResult] = {
    if(request.text.startsWith("FAKE")) {
      Future.successful(Json.parse(fakeEmoDetectionResult).as[EmotionDetectionResult])
    } else {
      makeApiCall(request).recoverWith {
        case e: Exception =>
          logger.error(s"Failed to detect emotion for request: $request", e)
          Future.failed(e)
      }
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
                  val emotionDetectionResult = Json.parse(content).as[EmotionDetectionResult]
                  aiService.saveAiResponseAsync(request.userId, response.json)
                  emotionDetectionResult
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