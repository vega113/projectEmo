package service.ai

import com.google.inject.ImplementedBy
import play.api.Configuration
import play.api.libs.json._
import play.api.libs.ws.WSClient

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.Try

@ImplementedBy(classOf[AiAssistantApiServiceImpl])
trait AiAssistantApiService {
  def makeApiGetCall[RespType : Reads](path: String)(implicit ec: ExecutionContext): Future[RespType]

  def makeApiPostCall[ReqType : Writes, RespType : Reads](path: String, request: ReqType)(implicit ec: ExecutionContext): Future[RespType]
  def makeApiPostCall[RespType : Reads](path: String)(implicit ec: ExecutionContext): Future[RespType]

  def makeApiDeleteCall[RespType : Reads](path: String)(implicit executionContext: ExecutionContext): Future[RespType]
}
class AiAssistantApiServiceImpl @Inject()(ws: WSClient, config: Configuration)
                                         (implicit ec: ExecutionContext) extends AiAssistantApiService {

  private lazy val logger = play.api.Logger(getClass)

  private lazy val apiKey = config.get[String]("openai.apikey")
  private lazy val timeout = config.get[Duration]("openai.timeout")
  private lazy val baseUrl = config.get[String]("openai.baseUrl")

  override def makeApiPostCall[RespType : Reads](path: String)(implicit ec: ExecutionContext): Future[RespType] = makeApiPostCall(path, "")
  override def makeApiPostCall[ReqType : Writes, RespType : Reads](path: String, request: ReqType)(implicit ec: ExecutionContext): Future[RespType] = {
    val headers = createHeaders(apiKey)
    val payload = Json.toJson(request)
     val urlStr = s"$baseUrl$path"
    logger.info(s"Making API POST call with payload: $payload, timeout: $timeout, url: $urlStr")
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
                  logger.trace(s"Deserialization successful: $result")
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
      }(ec)
  }

  override def makeApiDeleteCall[RespType : Reads](path: String)(implicit ec: ExecutionContext): Future[RespType] = {
    logger.info(s"Making API DELETE call with timeout: $timeout, url: $path")
    val headers = createHeaders(apiKey)
    val urlStr = s"$baseUrl$path"      
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
      }(ec)
  }

  private def createHeaders(apKey: String): Map[String, String] = {
    Map(
      "Content-Type" -> "application/json",
      "Authorization" -> s"Bearer $apKey",
      "OpenAI-Beta" -> "assistants=v1"
    )
  }

  override def makeApiGetCall[RespType: Reads](path: String)(implicit ec: ExecutionContext): Future[RespType] = {
    val headers = createHeaders(apiKey)
    val urlStr = s"$baseUrl$path"
    logger.info(s"Making API GET call with timeout: $timeout, url: $urlStr")
    ws.url(urlStr)
      .withRequestTimeout(timeout)
      .withHttpHeaders(headers.toSeq: _*)
      .get()
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
      }(ec)
  }
}
