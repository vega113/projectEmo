package controllers

import auth.AuthenticatedAction
import dao.model.{EmotionDetectionResult, EmotionFromNoteResult, EmotionRecord, Note, Trigger}
import liquibase.LiquibaseRunner
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import service.{EmotionDetectionService, EmotionRecordService, NoteService, TagService}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NoteController @Inject()(cc: ControllerComponents,
                               noteService: NoteService,
                               tagService: TagService,
                               emotionRecordService: EmotionRecordService,
                               emotionDetectionService: EmotionDetectionService,
                               authenticatedAction: AuthenticatedAction)
  extends AbstractController(cc){

  private val logger: Logger = LoggerFactory.getLogger(classOf[NoteController])


  def fetchNoteTemplate(): Action[AnyContent] = Action andThen authenticatedAction async {
    noteService.findAllNoteTemplates().map(noteTemplate => Ok(Json.toJson(noteTemplate)))
  }

  def deleteNote(id: Long): Action[AnyContent] =
    Action andThen authenticatedAction async { implicit token =>
      emotionRecordService.findEmotionRecordIdByUserIdNoteId(token.user.userId, id).flatMap {
        case Some(emotionRecordId) => noteService.delete(emotionRecordId, id).map {
          case true => Ok
          case false => BadRequest(Json.obj("message" -> s"Invalid note id: $id"))
        }
        case None => Future.successful(BadRequest(Json.obj("message" -> s"Invalid note id: $id")))
      }
  }

  def undeleteNote(id: Long): Action[AnyContent] =
    Action andThen authenticatedAction async { implicit token =>
      emotionRecordService.findEmotionRecordIdByUserIdNoteId(token.user.userId, id).flatMap {
        case Some(emotionRecordId) => noteService.undelete(emotionRecordId, id).map {
          case true => Ok
          case false => BadRequest(Json.obj("message" -> s"Invalid note id: $id"))
        }
        case None => Future.successful(BadRequest(Json.obj("message" -> s"Invalid note id: $id")))
      }
    }

  def detectEmotion(): Action[JsValue] = Action(parse.json) andThen authenticatedAction async { implicit token =>
    token.body.validate[Note].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
      },
      note => {
        emotionDetectionService.detectEmotion(note.text).map { resp =>
          Ok(Json.toJson(resp))
        }
      })
    }
}