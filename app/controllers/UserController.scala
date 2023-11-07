package controllers

import auth.AuthenticatedAction
import dao.{DatabaseExecutionContext, UserDaoImpl}
import dao.model.User
import org.slf4j.LoggerFactory
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import play.api.libs.json.{Json, _}
import service.UserService

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class UserController @Inject()(cc: ControllerComponents,
                               userService: UserService)
  extends AbstractController(cc) {

  private val logger = LoggerFactory.getLogger(classOf[UserController])

  def createUser(): Action[AnyContent] = Action.async { implicit request =>
    request.body.asJson.map { json =>
      logger.info("attempting to create user: " + json.toString())
      json.validate[User] match {
        case JsSuccess(user, _) => userService.insert(user).map {
          case Some(id) =>
            logger.info("user created: " + user.username)
            Created(Json.toJson(user.copy(userId = Some(id.toInt))))
          case None => Conflict("Failed to create user, probably due to duplicate username or email")
        }
        case JsError(errors) =>
          logger.info("failed to create user: " + errors.mkString(","))
          Future.failed(new IllegalArgumentException("Invalid user format: " + errors.mkString(", ")))
      }
    }.getOrElse {
      logger.warn("failed to parse request: " + request.body.asText)
      Future.successful(BadRequest("Expecting application/json request body"))
    }
  }

  def updateUser(id: Int): Action[AnyContent] = Action.async { implicit request =>
    request.body.asJson.map { json =>
      json.validate[User] match {
        case JsSuccess(user, _) => userService.update(user.copy(userId = Option(id))).map {
          case 1 => Ok(Json.toJson(user))
          case _ => NotFound(s"User with id $id not found")
        }
        case JsError(errors) => Future.failed(new IllegalArgumentException("Invalid user format: " + errors.mkString(", ")))
      }
    }.getOrElse {
      Future.successful(BadRequest("Expecting application/json request body"))
    }
  }

  def deleteUser(id: Int): Action[AnyContent] = Action.async { implicit request =>
    userService.delete(id).map {
      case 1 => Ok(s"User with id $id deleted")
      case _ => NotFound(s"User with id $id not found")
    }
  }
}
