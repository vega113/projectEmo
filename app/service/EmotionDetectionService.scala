package service

import com.google.inject.ImplementedBy
import dao.model.EmotionDetectionResult
import play.api.{Configuration, Logger}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.libs.ws.WSClient
import service.serviceModel.ChatGptApiResponse

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@ImplementedBy(classOf[ChatGptEmotionDetectionServiceImpl])
trait EmotionDetectionService {
  def detectEmotion(txt: String): Future[EmotionDetectionResult]
}

class ChatGptEmotionDetectionServiceImpl @Inject()(ws: WSClient, config: Configuration)(implicit ec: ExecutionContext) extends EmotionDetectionService {

  private final val logger: Logger = play.api.Logger(getClass)
  private final val fakeEmoDetectionResult = "{\"emotionType\":\"Positive\",\"intensity\":3,\"mainEmotionId\":\"Joy\",\"subEmotionId\":\"Serenity\",\"description\":\"Listening to Dada Istamaya's spiritual experience and feeling the inner silence, love, and beauty inspires you and brings you joy.\",\"suggestion\":\"Take a moment to reflect on the emotions and sensations you felt during the video. Explore ways to incorporate more moments of inner silence, love, and beauty into your own life, such as through meditation or engaging in activities that bring you joy and inspiration.\",\"triggers\":[{\"triggerName\":\"Other\"},{\"triggerName\":\"Spiritual experience\"}],\"tags\":[{\"tagName\":\"joy\"},{\"tagName\":\"inspiration\"},{\"tagName\":\"inner silence\"},{\"tagName\":\"love\"},{\"tagName\":\"beauty\"}]}"

  override def detectEmotion(txt: String): Future[EmotionDetectionResult] = {
    if(txt.startsWith("FAKE")) {
      Future.successful(Json.parse(fakeEmoDetectionResult).as[EmotionDetectionResult])
    } else {
      makeApiCall(txt)
    }
  }

  private def makeApiCall(userMessage: String): Future[EmotionDetectionResult] = {
    val headers = Map(
      "Content-Type" -> "application/json",
      "Authorization" -> s"Bearer ${config.get[String]("openai.apikey")}"
    )

    val payload = Json.obj(
      "model" -> config.get[String]("openai.model"),
      "messages" -> Json.arr(
        Json.obj(
          "role" -> "system",
          "content" -> config.get[String]("openai.systemPromt")
        ),
        Json.obj(
          "role" -> "user",
          "content" -> userMessage
        )
      ),
      "max_tokens" -> 2000,
      "temperature" -> 0.99
    )
    logger.info(s"Making API call with payload: $payload")

    // Make the API call
    ws.url(config.get[String]("openai.url"))
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
                  //TODO: validate the result, check that fields are not empty and are from the expected set
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
}
