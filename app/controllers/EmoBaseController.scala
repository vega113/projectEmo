package controllers

import auth.AuthenticatedAction
import auth.model.TokenData
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Result}

import scala.concurrent.Future

abstract class EmoBaseController(cc: ControllerComponents, authenticatedAction: AuthenticatedAction) extends AbstractController(cc) {
  def authenticatedActionWithUser[T](block: TokenData => Future[Result]): Action[AnyContent] = {
    Action andThen authenticatedAction async { implicit token =>
      block(token.user)
    }
  }
}
