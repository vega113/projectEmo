package service.ai

import com.google.inject.ImplementedBy
import play.api.libs.json._
import play.api.libs.ws.WSClient

import javax.inject.Inject
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@ImplementedBy(classOf[AiAssistantApiServiceImpl])
trait AiAssistantApiService {

  def createHeaders(apKey: String): Map[String, String]
  def makeApiPostCall[ReqType : Writes, RespType : Reads](request: ReqType, urlStr: String, timeout: Duration, apiKey: String): Future[RespType]

  def makeApiDeleteCall[RespType : Reads](urlStr: String, timeout: Duration, apiKey: String): Future[RespType]
}
class AiAssistantApiServiceImpl @Inject()(ws: WSClient)
                                         (implicit ec: ExecutionContext) extends AiAssistantApiService {

  private lazy val logger = play.api.Logger(getClass)
  override def makeApiPostCall[ReqType : Writes, RespType : Reads](request: ReqType, urlStr: String, timeout: Duration, apiKey: String): Future[RespType] = {
    val headers = createHeaders(apiKey)
    val payload = Json.toJson(request)
    logger.info(s"Making API call with payload: $payload, timeout: $timeout")

    // Make the API call
    ws.url(urlStr)
      .withRequestTimeout(timeout)
      .withHttpHeaders(headers.toSeq: _*)
      .post(payload)
      .flatMap { response =>
        if (response.status == 200) {
          Future.fromTry {
            Try {
              response.json.validate[RespType] match {
                case JsSuccess(result, _) =>
                  logger.info(s"Deserialization successful: $result")
                  result
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

  override def makeApiDeleteCall[RespType : Reads](urlStr: String, timeout: Duration, apiKey: String): Future[RespType] = {
    val headers = createHeaders(apiKey)
    ws.url(urlStr)
      .withRequestTimeout(timeout)
      .withHttpHeaders(headers.toSeq: _*)
      .delete()
      .flatMap { response =>
        if (response.status == 200) {
          Future.fromTry {
            Try {
              response.json.validate[RespType] match {
                case JsSuccess(result, _) =>
                  logger.info(s"Deserialization successful: $result")
                  result
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


  override def createHeaders(apKey: String): Map[String, String] = {
    Map(
      "Content-Type" -> "application/json",
      "Authorization" -> s"Bearer $apKey",
      "OpenAI-Beta" -> "assistants=v1"
    )
  }
}
