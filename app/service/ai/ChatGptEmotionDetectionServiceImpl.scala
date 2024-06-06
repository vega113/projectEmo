package service.ai

import dao.model.EmotionDetectionResult
import play.api.{Configuration, Logger}
import play.api.libs.json.{JsError, JsObject, JsSuccess, Json}
import play.api.libs.ws.WSClient
import service.model.DetectEmotionRequest
import service.serviceModel.ChatGptApiResponse

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.{Success, Try}

@Named("ChatGpt")
class ChatGptEmotionDetectionServiceImpl @Inject()(ws: WSClient, config: Configuration)(implicit ec: ExecutionContext) extends EmotionDetectionService {

  private final val logger: Logger = play.api.Logger(getClass)
  private final val fakeEmoDetectionResult = "{\"emotionType\":\"Positive\",\"intensity\":3,\"mainEmotionId\":\"Joy\",\"subEmotionId\":\"Serenity\",\"description\":\"Listening to Dada Istamaya's spiritual experience and feeling the inner silence, love, and beauty inspires you and brings you joy.\",\"suggestion\":\"Take a moment to reflect on the emotions and sensations you felt during the video. Explore ways to incorporate more moments of inner silence, love, and beauty into your own life, such as through meditation or engaging in activities that bring you joy and inspiration.\",\"triggers\":[{\"triggerName\":\"Other\"},{\"triggerName\":\"Spiritual experience\"}],\"tags\":[{\"tagName\":\"joy\"},{\"tagName\":\"inspiration\"},{\"tagName\":\"inner silence\"},{\"tagName\":\"love\"},{\"tagName\":\"beauty\"}]}"

  override def detectEmotion(request: DetectEmotionRequest): Future[EmotionDetectionResult] = {
    val responseFuture = if (request.text.startsWith("FAKE")) {
      Future.successful(Json.parse(fakeEmoDetectionResult).as[EmotionDetectionResult])
    } else {
      makeApiCall(request).recoverWith {
        case e: Exception =>
          logger.error(s"V1 Failed to detect emotion for request: $request", e)
          Future.failed(e)
      }
    }
    responseFuture
  }

  /**
   * Makes an API call to detect emotion based on the given request.
   */
  private[ai] def makeApiCall(request: DetectEmotionRequest): Future[EmotionDetectionResult] = {
    val headers = createHeaders

    val payload = createPayload(request)
    val timeoutDuration = config.get[Duration]("openai.timeout")
    logger.info(s"Making API call with payload: $payload, timeout: $timeoutDuration")

    val url = config.get[String]("openai.baseUrl") + "/v1/chat/completions"
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
                  logger.info(s"Deserialization successful: ${response.json}")
                  val origContent = result.choices.head.message.tool_calls.head.function.arguments
                  val content: String = discardStringPrefixAndExtractJustTheJson(origContent)
                  Try {
                    Json.parse(content).as[EmotionDetectionResult]
                  } match {
                    case scala.util.Success(value) =>
                      value
                    case scala.util.Failure(e) =>
                      logger.error(s"Failed to parse emotion detection result: $content", e)
                      throw new Exception(s"Failed to parse emotion detection result: $content")
                  }
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

  private def discardStringPrefixAndExtractJustTheJson(origContent: String): String = {
    // origContent starts with some text and then the actual JSON. We need to discard the text and extract just the JSON.
    // Find the index of the first '{' character
    val firstBraceIndex = origContent.indexOf('{')
    val lastBraceIndex = origContent.lastIndexOf('}')
    // Extract the JSON from the first '{' character to the end of the string
    origContent.substring(firstBraceIndex, lastBraceIndex + 1)
      // we also need to remove the end of line character at the end of the string
      .replace("\n", "")
      // we also need to unescape the double quotes
      .replace("\\\"", "\"")
      // we also need to replacer non standard quotes like “ and ”
      .replace("“", "\"")
      .replace("”", "\"")
      // we also need to replacer non standard quotes like ` and ´
      .replace("`", "'")
      .replace("´", "'")
  }

  private[ai] def createPayload(request: DetectEmotionRequest): JsObject = {
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
      "max_tokens" -> 4096,
      "temperature" -> 0.99,
      "tools" -> Json.arr(
        Json.obj(
          "type" -> "function",
          "function" -> Json.obj(
            "name" -> "save_ai_emotion_response",
            "description" -> "Save the AI emotion response",
            "parameters" -> Json.obj(
              "type" -> "object",
              "properties" -> Json.obj(
                "textTitle" -> Json.obj(
                  "type" -> "string",
                  "description" -> "The main idea of the text"
                ),
                "intensity" -> Json.obj(
                  "type" -> "integer",
                  "minimum" -> 0,
                  "maximum" -> 5
                ),
                "subEmotionId" -> Json.obj(
                  "type" -> "string",
                  "enum" -> Json.arr("Aggressiveness", "Annoyance", "Bitterness", "Frustration", "Fury", "Hatred", "Hostility", "Indignation", "Insult", "Irritability", "Nervousness", "Offense", "Resentment", "Disinterest", "Indifference", "Lethargy", "Arrogance", "Aversion", "Contempt", "Disapproval", "Disdain", "Distaste", "Loathing", "Nausea", "Repugnance", "Revulsion", "Self-satisfaction", "Agitation", "Alertness", "Anxiety", "Apprehension", "Awkwardness", "Concern", "Dread", "Fright", "Horror", "Insecurity", "Panic", "Sense of threat", "Suspicion", "Trepidation", "Uneasiness", "Worry", "Covetousness", "Longing", "Abandonment", "Alienation", "Apathy", "Dejection", "Depression", "Despair", "Desperation", "Devastation", "Disappointment", "Disorder", "Gloom", "Grief", "Heaviness", "Helplessness", "Hopelessness", "Infringement", "Isolation", "Listlessness", "Loneliness", "Melancholy", "Oppression", "Pain", "Sorrow", "Vulnerability", "Weakness", "Weariness", "Chagrin", "Disgrace", "Dishonor", "Embarrassment", "Guilt", "Humiliation", "Regret", "Remorse", "Shyness", "Audacity", "Boredom", "Decline of strength", "Determination", "Discomfort", "Dreaminess", "Exhaustion", "Incoherence", "Lostness", "Rebellion", "Restraint", "Sense of deadlock", "Sentimentality", "Seriousness", "Stupidity", "Tiredness", "Amazement", "Astonishment", "Bewilderment", "Confusion", "Defeat", "Disarray", "Disbelief", "Disorientation", "Dizziness", "Eagerness", "Fascination", "Inquisitiveness", "Intrigue", "Perplexity", "Shock", "Startlement", "Uncertainty", "Upset", "Wonder", "Curiosity", "Engagement", "Focus", "Hope", "Impatience", "Amusement", "Bliss", "Charm", "Contentment", "Elation", "Enthusiasm", "Euphoria", "Excitement", "Gratitude", "Happiness", "Optimism", "Passion", "Pleasure", "Pride", "Satisfaction", "Serenity", "Trembling", "Triumph", "Adoration", "Affection", "Fondness", "Infatuation", "Warmth", "Admiration", "Attachment", "Awe", "Calmness", "Comfort", "Compassion", "Confidence", "Dependability", "Dependence", "Faith", "Friendliness", "Generosity", "Loyalty", "Peacefulness", "Relaxation", "Relief", "Respect", "Security", "Sympathy", "Tenderness")
                ),
                "description" -> Json.obj(
                  "type" -> "string",
                  "description" -> "Explain what the user feels and why. The description should provide empathy and understanding, address the user directly"
                ),
                "suggestion" -> Json.obj(
                  "type" -> "string",
                  "description" -> "Provide helpful general advice based on the emotion detected, include explanation and reasons that justify suggestion. You can provide and extansive response here, if this can help. Adress the user directly"
                ),
                "triggers" -> Json.obj(
                  "type" -> "array",
                  "minItems" -> 1,
                  "items" -> Json.obj(
                    "type" -> "object",
                    "properties" -> Json.obj(
                      "triggerName" -> Json.obj(
                        "type" -> "string",
                        "description" -> "The trigger that caused the emotion. If the trigger is not listed, select 'Other' and provide a brief description in the description field.",
                        "enum" -> Json.arr("People", "Situations", "Places", "Ideas", "Other")
                      )
                    )
                  )
                ),
                "tags" -> Json.obj(
                  "type" -> "array",
                  "minItems" -> 1,
                  "items" -> Json.obj(
                    "type" -> "object",
                    "properties" -> Json.obj(
                      "tagName" -> Json.obj(
                        "type" -> "string"
                      )
                    )
                  )
                ),
                "todos" -> Json.obj(
                  "type" -> "array",
                  "items" -> Json.obj(
                    "type" -> "object",
                    "properties" -> Json.obj(
                      "title" -> Json.obj(
                        "type" -> "string"
                      ),
                      "description" -> Json.obj(
                        "type" -> "string"
                      ),
                      "type" -> Json.obj(
                        "type" -> "string"
                      )
                    ),
                    "required" -> Json.arr("title", "description", "type")
                  )
                )
              ),
              "required" -> Json.arr("textTitle", "intensity", "subEmotionId", "description", "suggestion", "triggers", "tags", "todos")
            ),
            "output" -> Json.obj(
              "type" -> "void"
            )
          )
        )
      ),
      "tool_choice" -> "required"
    )
  }

  private def createHeaders = {
    Map(
      "Content-Type" -> "application/json",
      "Authorization" -> s"Bearer ${config.get[String]("openai.apikey")}"
    )
  }
}
