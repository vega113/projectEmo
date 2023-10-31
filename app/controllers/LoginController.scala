package controllers

import auth.JwtService
import auth.model.LoginData
import org.slf4j.LoggerFactory
import play.api.Logger
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

  private val logger = LoggerFactory.getLogger(classOf[LoginController])

  def login: Action[LoginData] = Action.async(parse.json[LoginData]) { implicit request =>
    val loginData = request.body
    logger.info("logging attempt for:  " + loginData.username)
    userService.findByUsername(loginData.username).map {
      case Some(user) if user.password == loginData.password && user.username == loginData.username =>
        val token = jwtService.createToken(user, 365.days) // TODO make expiration configurable
        logger.info("login successful:  " + loginData.username)
        Ok(Json.obj("token" -> token))
      case _ =>
        logger.info("login failed:  " + loginData.username)
        Unauthorized(Json.obj("message" -> "Invalid username or password"))
    }
  }
}

