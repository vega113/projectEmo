package auth

import play.api.mvc.{Request, WrappedRequest}

object model {
  case class LoginData(username: String, password: String)
  case class TokenData(userId: Long, username: String, email: String, firstname: String, lastname: String, role: String = "user")

  class AuthenticatedRequest[A](val user: TokenData, request: Request[A]) extends WrappedRequest[A](request)

  object LoginData {
    import play.api.libs.json._
    implicit val loginDataFormat: OFormat[LoginData] = Json.format[LoginData]
  }

  object TokenData {
    import play.api.libs.json._
    implicit val tokenDataFormat: OFormat[TokenData] = Json.format[TokenData]
  }
}
