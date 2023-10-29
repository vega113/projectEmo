package controllers

import auth.AuthenticatedAction
import dao.model.{EmotionRecord, SubEmotion}
import play.api.libs.json._
import play.api.mvc._
import service.{EmotionRecordService, EmotionService}

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class EmotionController @Inject()(cc: ControllerComponents,
                                  emotionService: EmotionService,
                                  authenticatedAction: AuthenticatedAction)
  extends AbstractController(cc) {


  def findAll(): Action[AnyContent] = Action async { // TODO allow only for admins
    emotionService.findAll().map(emotions => Ok(Json.toJson(emotions)))
  }
}