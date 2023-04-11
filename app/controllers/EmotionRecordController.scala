package controllers

import auth.AuthenticatedAction
import dao.model.EmotionRecord
import play.api.libs.json._
import play.api.mvc._
import service.EmotionRecordService

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class EmotionRecordController @Inject()(cc: ControllerComponents,
                                        emotionRecordService: EmotionRecordService,
                                        authenticatedAction: AuthenticatedAction)
  extends AbstractController(cc) {

  def findAll(): Action[AnyContent] = Action andThen authenticatedAction async { // TODO allow only for admins
    emotionRecordService.findAll().map(emotionRecords => Ok(Json.toJson(emotionRecords)))
  }

  def findById(id: Long): Action[AnyContent] = Action andThen authenticatedAction async { implicit token =>
    emotionRecordService.findById(token.user.userId).map {
      case Some(emotionRecord) => Ok(Json.toJson(emotionRecord))
      case None => NotFound
    }
  }

  def insert(): Action[JsValue] = Action(parse.json) andThen authenticatedAction async { implicit token =>
    token.body.validate[EmotionRecord].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
      },
      emotionRecord => {
        emotionRecordService.insert(emotionRecord).map {
          case Some(id) => Created(Json.toJson(emotionRecord.copy(id = Option(id))))
          case None => InternalServerError
        }
      }
    )
  }

  def update(id: Long): Action[JsValue] = Action(parse.json) andThen authenticatedAction async { implicit token =>
    token.body.validate[EmotionRecord].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
      },
      emotionRecord => {
        emotionRecordService.update(emotionRecord.copy(id = Option(token.user.userId))).map {
          case 1 => Ok(Json.toJson(emotionRecord))
          case _ => NotFound
        }
      }
    )
  }

  def delete(id: Long): Action[AnyContent] = Action andThen authenticatedAction async  { implicit token =>
    emotionRecordService.delete(token.user.userId).map {
      case 1 => Ok
      case _ => NotFound
    }
  }

  def findAllByUserId(id: Long): Action[AnyContent] = Action andThen authenticatedAction async { implicit token =>
    emotionRecordService.findAllByUserId(token.user.userId).map(emotionRecords => Ok(Json.toJson(emotionRecords)))
  }
}
