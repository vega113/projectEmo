package service.enums

sealed trait EmotionType {
  def name: String
}

object EmotionType {
  case object Positive extends EmotionType {
    val name = "Positive"
  }
  case object Negative extends EmotionType {
    val name = "Negative"
  }
  case object Neutral extends EmotionType {
    val name = "Neutral"
  }

  lazy val toList: List[String] = List(Positive, Negative, Neutral).map(_.name)
}