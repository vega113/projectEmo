package controllers

import dao.model.{Emotion, SubEmotion, SuggestedAction, Trigger}
import play.api.libs.json.Json

object model {
  case class EmotionData(emotions: List[EmotionWithSubEmotions], triggers: List[Trigger])
  case class EmotionWithSubEmotions(emotion: Emotion, subEmotions: List[SubEmotionWithActions])
  case class SubEmotionWithActions(subEmotion: SubEmotion, suggestedActions: List[SuggestedAction])

  object EmotionData {
    implicit val subEmotionActionFormat = Json.format[SubEmotionWithActions]
    implicit val emotionSubEmotionFormat = Json.format[EmotionWithSubEmotions]
    implicit val emotionDataFormat = Json.format[EmotionData]
  }
}
