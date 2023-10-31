package controllers

import auth.JwtService
import auth.model.LoginData
import play.api.libs.json.Json
import play.api.mvc._
import service.UserService
import javax.inject._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class LoginController @Inject()(
                                 val cc: ControllerComponents,
                                 userService: UserService,
                                 jwtService: JwtService
                               ) extends AbstractController(cc) {


  def login: Action[LoginData] = Action.async(parse.json[LoginData]) { implicit request =>
    val loginData = request.body
    userService.findByUsername(loginData.username).map {
      case Some(user) if user.password == loginData.password && user.username == loginData.username =>
        val token = jwtService.createToken(user, 365.days) // TODO make expiration configurable
        Ok(Json.obj("token" -> token))
      case _ => Unauthorized(Json.obj("message" -> "Invalid username or password"))
    }
  }
}

