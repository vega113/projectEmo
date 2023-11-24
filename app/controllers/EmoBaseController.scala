package controllers

import auth.AuthenticatedAction
import auth.model.{AuthenticatedRequest, TokenData}
import play.api.libs.json.{JsError, JsPath, JsValue, Json, JsonValidationError}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Result}

import scala.concurrent.Future

abstract class EmoBaseController(cc: ControllerComponents, authenticatedAction: AuthenticatedAction) extends AbstractController(cc) {

  private lazy val logger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(this.getClass)
  def authenticatedActionWithUser[T](block: TokenData => Future[Result]): Action[AnyContent] = {
    Action andThen authenticatedAction async { implicit token =>
      block(token.user)
    }
  }

  lazy val handleError: (collection.Seq[(JsPath, collection.Seq[JsonValidationError])], String, AuthenticatedRequest[JsValue]) =>
    Future[Result] = (errors: collection.Seq[(JsPath, collection.Seq[JsonValidationError])],
                       objectName:String, token: AuthenticatedRequest[JsValue]) => {
    logger.info(s"Failed to parse {} when inserting, user: {}, errors: {}", objectName, token.user.userId,
      JsError.toJson(errors).toString())
    Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
  }
}
