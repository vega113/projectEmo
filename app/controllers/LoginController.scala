package controllers

import auth.model.LoginData
import auth.{AuthenticatedAction, JwtService}
import net.logstash.logback.argument.StructuredArguments._
import org.slf4j.LoggerFactory
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc._
import service.UserService

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

@Singleton
class LoginController @Inject()(
                                 val cc: ControllerComponents,
                                 userService: UserService,
                                 jwtService: JwtService,
                                 authenticatedAction: AuthenticatedAction,
                                 config: Configuration
                               ) extends EmoBaseController(cc, authenticatedAction) {

  private val logger = LoggerFactory.getLogger(classOf[LoginController])

  def login: Action[LoginData] = Action.async(parse.json[LoginData]) { implicit request =>
    val loginData = request.body
    logger.info("logging attempt {} ", value("username", loginData.username))
    userService.findByUsername(loginData.username).map {
      case Some(user) if user.password == loginData.password && user.username == loginData.username =>
        val token = jwtService.createToken(user, config.get[Duration]("emo.config.loginTokenExpirationTime"))
        logger.info("login successful: {}", value("username", loginData.username))
        Ok(Json.obj("token" -> token))
      case _ =>
        logger.info("login failed: {}", value("username", loginData.username))
        Unauthorized(Json.obj("message" -> "Invalid username or password"))
    }
  }

  def logout: Action[AnyContent] = authenticatedActionWithUser { implicit token =>
    logger.info("logging out user: {}" + value("username", token.username))
    throw new RuntimeException("Testing honeybadger")
    Future.successful(Ok(Json.obj("message" -> "Logged out")))
  }
}

