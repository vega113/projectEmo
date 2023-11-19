package controllers

import auth.AuthenticatedAction
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import service.UserTodoService

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global

class UserTodoController @Inject()(cc: ControllerComponents, userTodoService: UserTodoService,
                                   authenticatedAction: AuthenticatedAction)
  extends EmoBaseController(cc, authenticatedAction) {

  private val logger: Logger = LoggerFactory.getLogger(classOf[UserTodoController])

  def fetchUserTodos(page: Int, size: Int): Action[AnyContent] = { //TODO: Add pagination
    authenticatedActionWithUser { implicit token =>
      logger.info("Fetching user todos")
      userTodoService.fetchByUserId(token.userId).map(userTodos => Ok(Json.toJson(userTodos)))
    }
  }

  def complete(userTodoId: Long, isDone: Boolean): Action[AnyContent] = {
    authenticatedActionWithUser { implicit token =>
      if (isDone) {
        logger.info("Completing user todo {}", Map("userTodoId" -> userTodoId))
        userTodoService.complete(token.userId, userTodoId).map(userTodos => Ok(Json.toJson(userTodos)))
      } else {
        logger.info("Uncompleting user todo {}", Map("userTodoId" -> userTodoId))
        userTodoService.uncomplete(token.userId, userTodoId).map(userTodos => Ok(Json.toJson(userTodos)))
      }

    }
  }

  def archive(userTodoId: Long, isArchived: Boolean): Action[AnyContent] = {
    authenticatedActionWithUser { implicit token =>
      if (isArchived) {
        logger.info("Archiving user todo {}", Map("userTodoId" -> userTodoId))
        userTodoService.archive(token.userId, userTodoId).map(userTodos => Ok(Json.toJson(userTodos)))
      } else {
        logger.info("Unarchiving user todo {}", Map("userTodoId" -> userTodoId))
        userTodoService.unarchive(token.userId, userTodoId).map(userTodos => Ok(Json.toJson(userTodos)))
      }
    }
  }
}
