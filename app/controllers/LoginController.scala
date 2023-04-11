package controllers

import play.api.mvc.Flash
import auth.JwtService
import auth.model.LoginData
import play.api.libs.json.Json
import play.api.mvc._
import service.UserService

import javax.inject._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class LoginController @Inject() (
                                  userService: UserService,
                                  cc: ControllerComponents,
                                  jwtService: JwtService
                                )(implicit ec: ExecutionContext) extends AbstractController(cc) {


  def login: Action[LoginData] = Action.async(parse.json[LoginData]) { implicit request =>
    val loginData = request.body
    userService.findByUsername(loginData.username).flatMap {
      case Some(user) if user.password == loginData.password =>
        val token = jwtService.createToken(user, 1.hour)
        Future.successful(Ok(Json.obj("token" -> token)))
      case _ =>
        Future.successful(Forbidden(Json.obj("error" -> "invalid token"))
        )
    }
  }
}

