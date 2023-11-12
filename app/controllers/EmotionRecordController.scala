
package controllers

import auth.AuthenticatedAction
import controllers.model.EmotionData._
import controllers.model.TagData
import dao.model.{EmotionRecord, Note}
import net.logstash.logback.argument.StructuredArguments._
import play.api.libs.json._
import play.api.mvc._
import service.{DateTimeService, EmotionRecordService, NoteService, TagService}

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, YearMonth, ZonedDateTime}
import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util._


class EmotionRecordController @Inject()(cc: ControllerComponents,
                                        emotionRecordService: EmotionRecordService,
                                        noteService: NoteService,
                                        tagService: TagService,
                                        dateTimeService: DateTimeService,
                                        authenticatedAction: AuthenticatedAction)
  extends EmoBaseController(cc, authenticatedAction) {

  val logger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(this.getClass)


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

    logger.info("Inserting emotion record for user {} {}",
      value("userId", token.user.userId), value("record", token.body))

    token.body.validate[EmotionRecord].fold(
      errors => {
        logger.info(s"Failed to parse emotion record when inserting, user: ${token.user.userId}," +
          s" errors: ${JsError.toJson(errors).toString()}")
        Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
      },
      emotionRecord => {
        if (!validateRequestUserId(emotionRecord.userId, token.user.userId)) {
          logger.warn(s"Invalid user id. body id: ${emotionRecord.userId} token id: ${token.user.userId}")
          Future.successful(BadRequest(Json.obj("message" -> s"Invalid user id. body id: ${emotionRecord.userId} token id: ${token.user.userId}")))
        } else {
          emotionRecordService.insert(emotionRecord).flatMap {
            case Some(id) =>
              logger.info(s"Inserted emotion record for user: ${token.user.userId} recordId: $id")
              fetchRecord(id, token.user.userId).map(record => Ok(Json.toJson(record)))
            case None =>
              logger.error(s"Failed to insert emotion record for user: ${token.user.userId}")
              Future.successful(InternalServerError)
          }
        }
      }
    )
  }

  def insertNote(emotionRecordId: Long): Action[JsValue] = Action(parse.json) andThen authenticatedAction async { implicit token =>
    logger.info(s"Inserting note for user: ${token.user.userId} recordId: $emotionRecordId")
    token.body.validate[Note].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
      },
      note => {
        noteService.insert(emotionRecordId, note).flatMap {
          case Some(_) => fetchRecord(emotionRecordId, token.user.userId).map(record => Ok(Json.toJson(record)))
          case None => Future.successful(InternalServerError)
        }
      }
    )
  }

  def update(id: Long): Action[JsValue] = Action(parse.json) andThen authenticatedAction async { implicit token =>
    token.body.validate[EmotionRecord].fold(
      errors => {
        logger.info("errors when updating emotion record: " + JsError.toJson(errors).toString)
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

  def findAllByUserId(): Action[AnyContent] = Action andThen authenticatedAction async { implicit token =>
    logger.info("findAllByUserId")
    emotionRecordService.findAllByUserId(token.user.userId).map(emotionRecords => Ok(Json.toJson(emotionRecords)))
  }

  def findAllByUserIdAndDateRange(startDate: String, endDate: String): Action[AnyContent] =
    authenticatedActionWithUser { implicit token =>
      logger.info("findAllByUserIdAndDateRange")
      emotionRecordService.findAllByUserIdAndDateRange(token.userId, startDate, endDate).
        map(emotionRecords => Ok(Json.toJson(emotionRecords)))
    }

  def findRecordsByUserIdForMonth(monthStart: String, monthEnd: String): Action[AnyContent] =
    authenticatedActionWithUser { implicit token =>
      logger.info("findRecordsByUserIdForMonth")
      dateTimeService.parseMonthRange(Option(monthStart), Option(monthEnd)) match {
        case Success((from, to)) => emotionRecordService.fetchRecordsForMonthByDate(token.userId,
          from.toInstant, to.toInstant).map(
          emotionRecords => Ok(Json.toJson(emotionRecords)))
        case Failure(_) => Future.successful(BadRequest(Json.obj("message" -> "Invalid date format")))
      }
    }


  def findRecordsByDayByUserIdForMonth(monthStart: String, monthEnd: String): Action[AnyContent] =
    authenticatedActionWithUser { implicit token =>
      logger.info(s"entering findRecordsByDayByUserIdForMonth monthStart: $monthStart, monthEnd: $monthEnd")
      Try((ZonedDateTime.parse(monthStart), ZonedDateTime.parse(monthEnd))) match {
        case Success((from, to)) => emotionRecordService.fetchRecordsForMonthByDate(token.userId,
          from.toInstant, to.toInstant).map(emotionRecordService.groupRecordsByDate).
          map(emotionRecordService.generateLineChartTrendDataSetForEmotionTypesTriggers).
          map(emotionRecords => Ok(Json.toJson(emotionRecords)))
        case Failure(_) =>
          logger.info(s"failed to parse date for findRecordsByDayByUserIdForMonth monthStart: ${monthStart}, monthEnd: $monthEnd")
          Future.successful(BadRequest(Json.obj("message" -> "Invalid date format")))
      }
    }

  def findAllDaysByUserId(): Action[AnyContent] = Action andThen authenticatedAction async { implicit token =>
    logger.info("findAllDaysByUserId")
    emotionRecordService.findAllByUserId(token.user.userId).map(emotionRecordService.groupRecordsByDate).
      map(emotionRecords => Ok(Json.toJson(emotionRecords)))
  }

  def findAllByUserIdAndDateRangeForSunburstChart(startDate: String, endDate: String): Action[AnyContent] =
    authenticatedActionWithUser { token =>
      logger.info("findAllByUserIdAndDateRangeForSunburstChart")
      emotionRecordService.findAllByUserIdAndDateRange(token.userId, startDate, endDate).
        map(emotionRecordService.emotionRecordsToSunburstChartData).
        map(emotionRecordsChartData => Ok(Json.toJson(emotionRecordsChartData)))
    }

  def findAllByUserIdAndDateRangeForDoughnutEmotionTypeTriggersChart(startDate: String, endDate: String): Action[AnyContent] =
    authenticatedActionWithUser { token =>
      logger.info("findAllByUserIdAndDateRangeForDoughnutEmotionTypeTriggersChart, startDate: " + startDate + " endDate: " + endDate)
      emotionRecordService.findAllByUserIdAndDateRange(token.userId, startDate, endDate).
        map(emotionRecordService.emotionRecordsToDoughnutEmotionTypeTriggerChartData).
        map(emotionRecordsChartData => Ok(Json.toJson(emotionRecordsChartData)))
    }

  def deleteTag(id: Long): Action[AnyContent] =
    Action andThen authenticatedAction async { implicit token =>
      logger.info("deleting tag: " + id)
      emotionRecordService.findEmotionRecordIdByUserIdTagId(token.user.userId, id).flatMap {
        case Some(emotionRecordId) => tagService.delete(emotionRecordId, id).map {
          case true =>
            logger.info("deleted tag: " + id)
            Ok
          case false =>
            logger.info("failed to delete tag: " + id)
            BadRequest(Json.obj("message" -> s"Invalid tag id: $id"))
        }
        case None => Future.successful(BadRequest(Json.obj("message" -> s"Invalid tag id: $id")))
      }
    }

  def addTag(): Action[JsValue] = Action(parse.json) andThen authenticatedAction async { implicit token =>
    token.body.validate[TagData].fold(
      errors => {
        logger.info("failed to parse tag data: " + JsError.toJson(errors).toString)
        Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
      },
      tagData => {
        logger.info("adding tag: " + tagData.tagName)
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
