package controllers

import auth.AuthenticatedAction
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import service.{EmotionRecordService, NoteService}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NoteController @Inject()(cc: ControllerComponents,
                               noteService: NoteService,
                               emotionRecordService: EmotionRecordService,
                               authenticatedAction: AuthenticatedAction)
  extends AbstractController(cc){


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
}
