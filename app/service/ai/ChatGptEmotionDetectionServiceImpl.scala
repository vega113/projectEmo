package service.ai

import dao.model.EmotionDetectionResult
import io.cequence.openaiscala.domain.settings.CreateChatCompletionSettings
import io.cequence.openaiscala.domain.{FunctionCallSpec, FunctionSpec, SystemMessage, UserMessage}
import io.cequence.openaiscala.service.OpenAIService
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import service.model.DetectEmotionRequest

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Named("ChatGpt")
class ChatGptEmotionDetectionServiceImpl @Inject()(ws: WSClient, config: Configuration, openAiService: OpenAIService)(implicit ec: ExecutionContext) extends EmotionDetectionService {

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
      val tools: Seq[FunctionSpec] = createTools()

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
          val toolCalls = chatFunCompletionMessage.tool_calls.collect {
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

  private def createTools(): Seq[FunctionSpec] = {
    Seq(
      FunctionSpec(
        name = "save_ai_emotion_response",
        description = Some("Save the AI emotion response"),
        parameters = Map(
          "type" -> "object",
          "properties" -> Map(
            "textTitle" -> Map(
              "type" -> "string",
              "description" -> "The main idea of the text"
            ),
            "intensity" -> Map(
              "type" -> "integer",
              "minimum" -> 0,
              "maximum" -> 5
            ),
            "subEmotionId" -> Map(
              "type" -> "string",
              "enum" -> Seq("Aggressiveness", "Annoyance", "Bitterness", "Frustration", "Fury", "Hatred", "Hostility", "Indignation", "Insult", "Irritability", "Nervousness", "Offense", "Resentment", "Disinterest", "Indifference", "Lethargy", "Arrogance", "Aversion", "Contempt", "Disapproval", "Disdain", "Distaste", "Loathing", "Nausea", "Repugnance", "Revulsion", "Self-satisfaction", "Agitation", "Alertness", "Anxiety", "Apprehension", "Awkwardness", "Concern", "Dread", "Fright", "Horror", "Insecurity", "Panic", "Sense of threat", "Suspicion", "Trepidation", "Uneasiness", "Worry", "Covetousness", "Longing", "Abandonment", "Alienation", "Apathy", "Dejection", "Depression", "Despair", "Desperation", "Devastation", "Disappointment", "Disorder", "Gloom", "Grief", "Heaviness", "Helplessness", "Hopelessness", "Infringement", "Isolation", "Listlessness", "Loneliness", "Melancholy", "Oppression", "Pain", "Sorrow", "Vulnerability", "Weakness", "Weariness", "Chagrin", "Disgrace", "Dishonor", "Embarrassment", "Guilt", "Humiliation", "Regret", "Remorse", "Shyness", "Audacity", "Boredom", "Decline of strength", "Determination", "Discomfort", "Dreaminess", "Exhaustion", "Incoherence", "Lostness", "Rebellion", "Restraint", "Sense of deadlock", "Sentimentality", "Seriousness", "Stupidity", "Tiredness", "Amazement", "Astonishment", "Bewilderment", "Confusion", "Defeat", "Disarray", "Disbelief", "Disorientation", "Dizziness", "Eagerness", "Fascination", "Inquisitiveness", "Intrigue", "Perplexity", "Shock", "Startlement", "Uncertainty", "Upset", "Wonder", "Curiosity", "Engagement", "Focus", "Hope", "Impatience", "Amusement", "Bliss", "Charm", "Contentment", "Elation", "Enthusiasm", "Euphoria", "Excitement", "Gratitude", "Happiness", "Optimism", "Passion", "Pleasure", "Pride", "Satisfaction", "Serenity", "Trembling", "Triumph", "Adoration", "Affection", "Fondness", "Infatuation", "Warmth", "Admiration", "Attachment", "Awe", "Calmness", "Comfort", "Compassion", "Confidence", "Dependability", "Dependence", "Faith", "Friendliness", "Generosity", "Loyalty", "Peacefulness", "Relaxation", "Relief", "Respect", "Security", "Sympathy", "Tenderness")
            ),
            "description" -> Map(
              "type" -> "string",
              "description" -> "Explain what the user feels and why. The description should provide empathy and understanding, address the user directly"
            ),
            "suggestion" -> Map(
              "type" -> "string",
              "description" -> "Provide helpful general advice based on the emotion detected, include explanation and reasons that justify suggestion. You can provide and extansive response here, if this can help. Adress the user directly"
            ),
            "triggers" -> Map(
              "type" -> "array",
              "minItems" -> 1,
              "items" -> Map(
                "type" -> "object",
                "properties" -> Map(
                  "triggerName" -> Map(
                    "type" -> "string",
                    "description" -> "The trigger that caused the emotion. If the trigger is not listed, select 'Other' and provide a brief description in the description field.",
                    "enum" -> Seq("People", "Situations", "Places", "Ideas", "Other")
                  )
                )
              )
            ),
            "tags" -> Map(
              "type" -> "array",
              "description" -> "A few tags that describe the emotion. Could  be name of a person/place etc...",
              "minItems" -> 1,
              "items" -> Map(
                "type" -> "object",
                "properties" -> Map(
                  "tagName" -> Map(
                    "type" -> "string"
                  )
                )
              )
            ),
            "todos" -> Map(
              "type" -> "array",
              "description" -> "List of todos that the user should do to remedy the situation or in order to find solution or improve. If there are no todos, provide an empty array. Make sure the todo to be concise and specific. Make sure the todos in the list are actionable and can be completed in a reasonable amount of time. Do not repeat similar todos.",
              "minItems" -> 0,
              "maxItems" -> 5,
              "items" -> Map(
                "type" -> "object",
                "properties" -> Map(
                  "title" -> Map(
                    "type" -> "string"
                  ),
                  "description" -> Map(
                    "type" -> "string"
                  ),
                  "type" -> Map(
                    "type" -> "string"
                  )
                ),
                "required" -> Seq("title", "description", "type")
              )
            ),
          )
        ),
      )
    )
  }
}
