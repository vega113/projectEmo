package controllers

import auth.AuthenticatedAction
import play.api.libs.json._
import play.api.mvc._
import service.SubEmotionService

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global


class SubEmotionController @Inject()(cc: ControllerComponents,
                                     subEmotionService: SubEmotionService,
                                     authenticatedAction: AuthenticatedAction)
  extends AbstractController(cc) {


    def findAllByEmotionId(emotionId: String): Action[AnyContent] = Action async { // TODO allow only for admins
      subEmotionService.findAllByEmotionId(emotionId).map(subEmotions => Ok(Json.toJson(subEmotions)))
    }
}