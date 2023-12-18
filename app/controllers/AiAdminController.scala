package controllers

import auth.AuthenticatedAction
import dao.model.User
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import service.UserService
import service.ai.AiAdminService
import service.model.EmoCreateAssistantRequest

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AiAdminController @Inject()(cc: ControllerComponents,
                                  userService: UserService,
                                  aiAdminService: AiAdminService,
                                  authenticatedAction: AuthenticatedAction)
  extends EmoBaseController(cc, authenticatedAction){
  private lazy val logger = play.api.Logger(getClass)
  def createAssistant(): Action[JsValue] = Action(parse.json) andThen authenticatedAction async { implicit token =>
    logger.info("Creating assistant")
    token.body.validate[EmoCreateAssistantRequest].fold(
      errors => handleError(errors, "aiAssistant", token),
      createAssistantRequest => {
        userService.findById(token.user.userId).flatMap {
          case Some(User(_,_,_,_,_,_,_,_,Some(true))) =>
            aiAdminService.createAssistant(createAssistantRequest).map { assistant =>
              Ok(Json.toJson(assistant))
            }
          case _ =>
            val message = s"Invalid user id : ${token.user.userId} or user is not an admin"
            Future.successful(BadRequest(Json.obj("message" -> message)))
        }
      }
    )
  }

  def deleteAssistantByExternal(externalId: String): Action[JsValue] = Action(parse.json) andThen authenticatedAction async { implicit token =>
    logger.info("Deleting assistant")
    userService.findById(token.user.userId).flatMap {
      case Some(User(_,_,_,_,_,_,_,_,Some(true))) =>
        aiAdminService.deleteAssistantByExternalId(externalId).map { resp =>
          Ok(Json.toJson(resp))
        }
      case _ =>
        val message = s"Invalid user id : ${token.user.userId} or user is not an admin"
        Future.successful(BadRequest(Json.obj("message" -> message)))
    }
  }
}
