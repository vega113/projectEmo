
package controllers

import auth.AuthenticatedAction
import controllers.model.TagData
import dao.model.{EmotionRecord, Note}
import play.api.libs.json._
import play.api.mvc._
import service.{EmotionRecordService, NoteService, TagService}

import java.time.ZonedDateTime
import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util._
import model.EmotionData._


class EmotionRecordController @Inject()(cc: ControllerComponents,
                                        emotionRecordService: EmotionRecordService,
                                        noteService: NoteService,
                                        tagService: TagService,
                                        authenticatedAction: AuthenticatedAction)
  extends AbstractController(cc) {

  def findById(id: Long): Action[AnyContent] = Action andThen authenticatedAction async { implicit token =>
    emotionRecordService.findByIdForUser(id, token.user.userId).map {
      case Some(emotionRecord) => Ok(Json.toJson(emotionRecord))
      case None => NotFound
    }
  }

  private def validateRequestUserId(bodyUserId: Option[Long], tokenUserId: Long): Boolean = {
    bodyUserId match {
      case Some(id) => id == tokenUserId
      case None => true
    }
  }

  private def fetchRecord(id: Long, userId: Long): Future[EmotionRecord] = {
    emotionRecordService.findByIdForUser(id, userId).flatMap {
      case Some(emotionRecord) => Future.successful(emotionRecord)
      case None =>
        Future.failed[EmotionRecord](new RuntimeException(
          s"Record not found recordId: $id userId: $userId"))
    }
  }

  def insert(): Action[JsValue] = Action(parse.json) andThen authenticatedAction async { implicit token =>
    token.body.validate[EmotionRecord].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
      },
      emotionRecord => {
        if (!validateRequestUserId(emotionRecord.userId, token.user.userId)) {
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

  def insertNote(emotionRecordId: Long): Action[JsValue] = Action(parse.json) andThen authenticatedAction async { implicit token =>
    token.body.validate[Note].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
      },
      note => {

        noteService.insert(emotionRecordId, note).flatMap {
          case Some(id) => fetchRecord(emotionRecordId, token.user.userId).map(record => Ok(Json.toJson(record)))
          case None => Future.successful(InternalServerError)
        }
      }
    )
  }

  def fetchSuggestions(emotionRecordId: Long): Action[AnyContent] = Action andThen authenticatedAction async { implicit token =>
    fetchRecord(emotionRecordId, token.user.userId).flatMap { record =>
      emotionRecordService.findSuggestionsByEmotionRecord(record).
        map(suggestions => Ok(Json.toJson(suggestions)))
    }
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

  def delete(id: Long): Action[AnyContent] = Action andThen authenticatedAction async { implicit token =>
    emotionRecordService.delete(token.user.userId).map {
      case 1 => Ok
      case _ => NotFound
    }
  }

  def findAllByUserId(): Action[AnyContent] = Action andThen authenticatedAction async { implicit token =>
    emotionRecordService.findAllByUserId(token.user.userId).map(emotionRecords => Ok(Json.toJson(emotionRecords)))
  }

  def findAllByUserIdAndDateRange(startDate: String, endDate: String): Action[AnyContent] =
    Action andThen authenticatedAction async { implicit token =>
      emotionRecordService.findAllByUserIdAndDateRange(token.user.userId, startDate, endDate).
        map(emotionRecords => Ok(Json.toJson(emotionRecords)))
    }

  def findRecordsByUserIdForMonth(monthStart: String, monthEnd: String): Action[AnyContent] =
    Action andThen authenticatedAction async { implicit token =>
      Try((ZonedDateTime.parse(monthStart), ZonedDateTime.parse(monthEnd))) match {
        case Success((from, to)) => emotionRecordService.fetchRecordsForMonthByDate(token.user.userId,
          from.toInstant, to.toInstant).map(
          emotionRecords => Ok(Json.toJson(emotionRecords)))
        case Failure(_) => Future.successful(BadRequest(Json.obj("message" -> "Invalid date format")))
      }
    }


  def findAllDaysByUserId(): Action[AnyContent] = Action andThen authenticatedAction async { implicit token =>
    emotionRecordService.findAllByUserId(token.user.userId).map(emotionRecordService.groupRecordsByDate).
      map(emotionRecords => Ok(Json.toJson(emotionRecords)))
  }


  def findAllByUserIdAndDateRangeForCharts(startDate: String, endDate: String): Action[AnyContent] =
    Action andThen authenticatedAction async { implicit token =>
      emotionRecordService.findAllByUserIdAndDateRange(token.user.userId, startDate, endDate).
        map(emotionRecordService.emotionRecordsToChartData).
        map(emotionRecordsChartData => Ok(Json.toJson(emotionRecordsChartData)))
    }

  def deleteTag(id: Long): Action[AnyContent] =
    Action andThen authenticatedAction async { implicit token =>
      emotionRecordService.findEmotionRecordIdByUserIdTagId(token.user.userId, id).flatMap {
        case Some(emotionRecordId) => tagService.delete(emotionRecordId, id).map {
          case true => Ok
          case false => BadRequest(Json.obj("message" -> s"Invalid tag id: $id"))
        }
        case None => Future.successful(BadRequest(Json.obj("message" -> s"Invalid tag id: $id")))
      }
    }

  def addTag(): Action[JsValue] = Action(parse.json) andThen authenticatedAction async { implicit token =>
    token.body.validate[TagData].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
      },
      tagData => {
        emotionRecordService.findByIdForUser(tagData.emotionRecordId, token.user.userId).flatMap {
          case Some(_) => tagService.add(tagData.emotionRecordId, tagData.tagName).map {
            case true => Ok
            case false => BadRequest(Json.obj("message" -> s"Invalid tag name: ${tagData.tagName}"))
          }
          case None => Future.successful(BadRequest(Json.obj("message" -> s"Invalid tag id: ${tagData.tagName}")))
        }
      }
    )
  }
}
