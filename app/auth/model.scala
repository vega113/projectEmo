package auth

object model {
  case class LoginData(username: String, password: String)
  case class TokenData(username: String, email: String, firstname: String, lastname: String, role: String = "user")

  object LoginData {
    import play.api.libs.json._
    implicit val loginDataFormat: OFormat[LoginData] = Json.format[LoginData]
  }

  object TokenData {
    import play.api.libs.json._
    implicit val tokenDataFormat: OFormat[TokenData] = Json.format[TokenData]
  }
}
