package service.ai

import io.cequence.openaiscala.domain.FunctionSpec

trait FunctionTools {
  lazy val emoTools: Seq[FunctionSpec] = Seq(
    FunctionSpec(
      name = "detect_emotion",
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
