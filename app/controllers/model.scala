package controllers

import dao.model.{Emotion, SubEmotion, SuggestedAction, Trigger}
import play.api.libs.json.{Json, OFormat}

object model {
  case class EmotionData(emotionTypes: List[EmotionTypesWithEmotions], triggers: List[Trigger])
  case class EmotionTypesWithEmotions(emotionType: String, emotions: List[EmotionWithSubEmotions])
  case class EmotionWithSubEmotions(emotion: Emotion, subEmotions: List[SubEmotionWrapper])
  case class SubEmotionWrapper(subEmotion: SubEmotion, suggestedActions: List[SuggestedAction])
  case class TagData(tagName: String, emotionRecordId: Long)



  object EmotionData {
    implicit val tagDataFormat: OFormat[TagData] = Json.format[TagData]
    implicit val subEmotionActionFormat: OFormat[SubEmotionWrapper] = Json.format[SubEmotionWrapper]
    implicit val emotionSubEmotionFormat: OFormat[EmotionWithSubEmotions] = Json.format[EmotionWithSubEmotions]
    implicit val emotionTypesWithEmotionsFormat: OFormat[EmotionTypesWithEmotions] = Json.format[EmotionTypesWithEmotions]
    implicit val emotionDataFormat: OFormat[EmotionData] = Json.format[EmotionData]
  }
}
