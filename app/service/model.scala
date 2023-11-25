package service

object model {
  case class DetectEmotionRequest(text: String, userId: Long)
}
