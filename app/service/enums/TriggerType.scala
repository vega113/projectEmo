package service.enums

sealed trait TriggerType {
  def name: String
}

object TriggerType {
  case object People extends TriggerType {
    val name = "People"
  }
  case object Places extends TriggerType {
    val name = "Places"
  }
  case object Situations extends TriggerType {
    val name = "Situations"
  }
  case object Other extends TriggerType {
    val name = "Other"
  }
  case object Empty extends TriggerType {
    val name = "Empty"
  }

  lazy val toList: List[String] = List(People, Places, Situations, Other, Empty).map(_.name)
}
