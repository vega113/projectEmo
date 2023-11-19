package service.enums

sealed trait ColorType {

  def name: String
  def value: String
}

object ColorType {
  case object PeopleColor extends ColorType {
    val name = "People"
    val value = "#FF6B6B"
  }
  case object PlacesColor extends ColorType {
    val name = "Places"
    val value = "#4ECDC4"
  }
  case object SituationsColor extends ColorType {
    val name = "Situations"
    val value = "#FFD166"
  }
  case object OtherColor extends ColorType {
    val name = "Other"
    val value = "#839788"
  }
  case object EmptyColor extends ColorType {
    val name = "Empty"
    val value = "#D3D3D3"
  }
  case object Positive extends ColorType {
    val name = "Positive"
    val value = "#3f51b5"
  }
  case object Negative extends ColorType {
    val name = "Negative"
    val value = "#e57373"
  }
  case object Neutral extends ColorType {
    val name = "Neutral"
    val value = "#ffb74d"
  }

  def fromName(name: String): Option[ColorType] = name match {
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
}
