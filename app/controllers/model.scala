package controllers

import dao.model.{Emotion, SubEmotion, SuggestedAction, Trigger}
import play.api.libs.json.Json

object model {
  case class EmotionData(emotionTypes: List[EmotionTypesWithEmotions], triggers: List[Trigger])
  case class EmotionTypesWithEmotions(emotionType: String, emotions: List[EmotionWithSubEmotions])
  case class EmotionWithSubEmotions(emotion: Emotion, subEmotions: List[SubEmotionWithActions])
  case class SubEmotionWithActions(subEmotion: SubEmotion, suggestedActions: List[SuggestedAction])
  case class TagData(tagName: String, emotionRecordId: Long)

  object EmotionData {
    implicit val tagDataFormat = Json.format[TagData]
    implicit val subEmotionActionFormat = Json.format[SubEmotionWithActions]
    implicit val emotionSubEmotionFormat = Json.format[EmotionWithSubEmotions]
    implicit val emotionTypesWithEmotionsFormat = Json.format[EmotionTypesWithEmotions]
    implicit val emotionDataFormat = Json.format[EmotionData]
  }
}
