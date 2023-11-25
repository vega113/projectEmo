package controllers

import auth.AuthenticatedAction
import dao.model.Note
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import service.{EmotionDetectionService, EmotionRecordService, NoteService, NoteTodoService}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import net.logstash.logback.argument.StructuredArguments._
import service.model.DetectEmotionRequest

import scala.util.Success


class NoteController @Inject()(cc: ControllerComponents,
                               noteService: NoteService,
                               emotionRecordService: EmotionRecordService,
                               emotionDetectionService: EmotionDetectionService,
                               noteTodoService: NoteTodoService,
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
    logger.info("Detecting emotion")
    token.body.validate[Note].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
      },
      note => {
        emotionDetectionService.detectEmotion(DetectEmotionRequest(note.text, token.user.userId)).map { resp =>
          Ok(Json.toJson(resp))
        }
      })
    }

  def acceptTodo(noteTodoId: Long):Action[AnyContent] =
    Action andThen authenticatedAction async { implicit token =>
      noteTodoService.acceptNoteTodo(token.user.userId, noteTodoId).flatMap {
        case true =>
          logger.info("Accepted note todo {}", value("noteTodoId", noteTodoId))
          Future.successful(Ok)
        case false =>
          logger.error("Failed to accept note todo {}", value("noteTodoId", noteTodoId))
          Future.successful(BadRequest(Json.obj("message" -> s"Invalid note todo id: $noteTodoId")))
      }
  }
}