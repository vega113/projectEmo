package service.ai

import dao.model.EmotionDetectionResult
import io.cequence.openaiscala.domain.settings.CreateChatCompletionSettings
import io.cequence.openaiscala.domain.{FunctionCallSpec, FunctionSpec, SystemMessage, UserMessage}
import io.cequence.openaiscala.service.OpenAIService
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import service.model.DetectEmotionRequest

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Named("ChatGpt")
class ChatGptEmotionDetectionServiceImpl @Inject()(config: Configuration, openAiService: OpenAIService)(implicit ec: ExecutionContext) extends EmotionDetectionService with FunctionTools {

  private final val logger: Logger = play.api.Logger(getClass)
  private final val fakeEmoDetectionResult = "{\"emotionType\":\"Positive\",\"intensity\":3,\"mainEmotionId\":\"Joy\",\"subEmotionId\":\"Serenity\",\"description\":\"Listening to Dada Istamaya's spiritual experience and feeling the inner silence, love, and beauty inspires you and brings you joy.\",\"suggestion\":\"Take a moment to reflect on the emotions and sensations you felt during the video. Explore ways to incorporate more moments of inner silence, love, and beauty into your own life, such as through meditation or engaging in activities that bring you joy and inspiration.\",\"triggers\":[{\"triggerName\":\"Other\"},{\"triggerName\":\"Spiritual experience\"}],\"tags\":[{\"tagName\":\"joy\"},{\"tagName\":\"inspiration\"},{\"tagName\":\"inner silence\"},{\"tagName\":\"love\"},{\"tagName\":\"beauty\"}]}"

  override def detectEmotion(request: DetectEmotionRequest): Future[EmotionDetectionResult] = {
    val responseFuture = if (request.text.startsWith("FAKE")) {
      Future.successful(Json.parse(fakeEmoDetectionResult).as[EmotionDetectionResult])
    } else {
      val messages = Seq(
        SystemMessage(config.get[String]("openai.systemPromt")),
        UserMessage(request.text)
      )
      val tools: Seq[FunctionSpec] = emoTools

      val out: Future[EmotionDetectionResult] = openAiService
        .createChatToolCompletion(
          messages = messages,
          tools = tools,
          responseToolChoice = None, // means "auto"
          settings = CreateChatCompletionSettings(config.get[String]("openai.model"), temperature = Some(0.99),
            max_tokens = Some(4096))
        )
        .map { response =>
          val chatFunCompletionMessage = response.choices.head.message
          val toolCalls: Seq[(String, FunctionCallSpec)] = chatFunCompletionMessage.tool_calls.collect {
            case (id, x: FunctionCallSpec) => (id, x)
          }
          val content = toolCalls.map(_._2.arguments).head
          Try {
            Json.parse(content).as[EmotionDetectionResult]
          } match {
            case scala.util.Success(value) =>
              value
            case scala.util.Failure(e) =>
              logger.error(s"Failed to parse emotion detection result: $content", e)
              throw new Exception(s"Failed to parse emotion detection result: $content")
          }
        }
      out
    }
    responseFuture
  }
}
