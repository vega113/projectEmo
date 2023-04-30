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
    emotionRecordService.findByIdForUser(id, token.user.userId).map {
      case Some(emotionRecord) => Ok(Json.toJson(emotionRecord))
      case None => NotFound
    }
  }

  private def validateUserId(bodyUserId: Option[Long], tokenUserId: Long): Boolean = {
    bodyUserId match {
      case Some(id) => id == tokenUserId
      case None => true
    }
  }

  private def fetchRecord(id: Long, userId: Long): Future[EmotionRecord] = {
    emotionRecordService.findByIdForUser(id, userId).map {
      case Some(emotionRecord) => emotionRecord
      case None => throw new RuntimeException(s"Record not found recordId: $id userId: $userId")
    }
  }

  def insert(): Action[JsValue] = Action(parse.json) andThen authenticatedAction async { implicit token =>
    token.body.validate[EmotionRecord].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
      },
      emotionRecord => {
        if (!validateUserId(emotionRecord.userId, token.user.userId)) {
          Future.successful(BadRequest(Json.obj("message" -> s"Invalid user id. body id: ${emotionRecord.userId} token id: ${token.user.userId}")))
        } else {
          emotionRecordService.insert(emotionRecord).flatMap {
            case Some(id) => fetchRecord(id, token.user.userId).map(record => Ok(Json.toJson(record)))
            case None => Future.successful(InternalServerError)
          }
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
