package controllers

import auth.AuthenticatedAction
import play.api.libs.json._
import play.api.mvc._
import service.{SubEmotionService, TriggerService}

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global


class TriggerController @Inject()(cc: ControllerComponents,
                                  triggerService: TriggerService)
  extends AbstractController(cc) {


    def findAll(): Action[AnyContent] = Action async { // TODO allow only for admins
      triggerService.findAll().map(triggers => Ok(Json.toJson(triggers)))
    }
}
