package service

sealed trait Color {

  def name: String
  def value: String
}

object Color {
  case object PeopleColor extends Color {
    val name = "People"
    val value = "#FF6B6B"
  }
  case object PlacesColor extends Color {
    val name = "Places"
    val value = "#4ECDC4"
  }
  case object SituationsColor extends Color {
    val name = "Situations"
    val value = "#FFD166"
  }
  case object OtherColor extends Color {
    val name = "Other"
    val value = "#839788"
  }
  case object EmptyColor extends Color {
    val name = "Empty"
    val value = "#D3D3D3"
  }
  case object Positive extends Color {
    val name = "Positive"
    val value = "#3f51b5"
  }
  case object Negative extends Color {
    val name = "Negative"
    val value = "#e57373"
  }
  case object Neutral extends Color {
    val name = "Neutral"
    val value = "#ffb74d"
  }

  def fromName(name: String): Option[Color] = name match {
    case "People" => Some(PeopleColor)
    case "Places" => Some(PlacesColor)
    case "Situations" => Some(SituationsColor)
    case "Other" => Some(OtherColor)
    case "Empty" => Some(EmptyColor)
    case "Positive" => Some(Positive)
    case "Negative" => Some(Negative)
    case "Neutral" => Some(Neutral)
    case _ => Some(Neutral)
  }

  def toMap: Map[String, String] = Map(
    "People" -> PeopleColor.value,
    "Places" -> PlacesColor.value,
    "Situations" -> SituationsColor.value,
    "Other" -> OtherColor.value,
    "Empty" -> EmptyColor.value,
    "Positive" -> Positive.value,
    "Negative" -> Negative.value,
    "Neutral" -> Neutral.value
  )

  def triggerColors: List[Color] = List(PeopleColor, PlacesColor, SituationsColor, OtherColor, EmptyColor)
  def emotionTypeColors: List[Color] = List(Positive, Negative, Neutral)
}


trait ColorsService {

}
