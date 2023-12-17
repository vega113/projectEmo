package controllers

import auth.AuthenticatedAction
import dao.model.Note
import net.logstash.logback.argument.StructuredArguments._
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import service.ai.{EmoDetectionServiceWithAssistant, EmotionDetectionService}
import service.model.DetectEmotionRequest
import service.{NoteService, NoteTodoService}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class NoteController @Inject()(cc: ControllerComponents,
                               noteService: NoteService,
                               emotionDetectionService: EmotionDetectionService,
                               emotionDetectionServiceV2: EmoDetectionServiceWithAssistant,
                               noteTodoService: NoteTodoService,
                               authenticatedAction: AuthenticatedAction)
  extends AbstractController(cc){

  private val logger: Logger = LoggerFactory.getLogger(classOf[NoteController])


  def fetchNoteTemplate(): Action[AnyContent] = Action andThen authenticatedAction async {
    noteService.findAllNoteTemplates().map(noteTemplate => Ok(Json.toJson(noteTemplate)))
  }

  def deleteNote(id: Long): Action[AnyContent] =
    Action andThen authenticatedAction async { implicit token =>
      noteService.delete(token.user.userId, id).map {
        case true => Ok
        case false => BadRequest(Json.obj("message" -> s"Invalid note id: $id"))
      }
  }

  def detectEmotion(): Action[JsValue] = Action(parse.json) andThen authenticatedAction async { implicit token =>
    logger.info("Detecting emotion")
    token.body.validate[Note].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
      },
      note => {
        Try{
          emotionDetectionServiceV2.detectEmotion(DetectEmotionRequest(note.text, token.user.userId)).onComplete(
            {
              case scala.util.Success(value) => logger.info(s"Successfully detected emotion v2: $value")
              case scala.util.Failure(exception) => logger.error(s"Failed to detect emotion v2: $exception", exception)
            }
          )
        }
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