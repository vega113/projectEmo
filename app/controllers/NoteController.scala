package controllers

import auth.AuthenticatedAction
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import service.NoteService

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global

class NoteController @Inject()(cc: ControllerComponents,
                               noteService: NoteService,
                               authenticatedAction: AuthenticatedAction)
  extends AbstractController(cc){


  def fetchNoteTemplate(): Action[AnyContent] = Action andThen authenticatedAction async {
    noteService.findAllNoteTemplates().map(noteTemplate => Ok(Json.toJson(noteTemplate)))
  }
}
