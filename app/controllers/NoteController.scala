package controllers

import auth.{AuthenticatedAction, model}
import dao.model.{EmotionDetectionResult, EmotionRecord, Note}
import net.logstash.logback.argument.StructuredArguments._
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import service.ai.EmotionDetectionServiceWithIdempotency
import service.model.DetectEmotionRequest
import service.{EmotionRecordService, NoteService, NoteTodoService}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NoteController @Inject()(cc: ControllerComponents,
                               noteService: NoteService,
                               emotionDetectionService: EmotionDetectionServiceWithIdempotency,
                               noteTodoService: NoteTodoService,
                               emotionRecordService: EmotionRecordService,
                               authenticatedAction: AuthenticatedAction)
  extends AbstractController(cc) {

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
    token.body.validate[Note].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
      },
      note => {
        logger.info(s"Detecting emotion for note ${note.id}")
        token.headers.get("X-IdempotencyKey")
        val v1EmotionFuture = emotionDetectionService.detectEmotion(DetectEmotionRequest(note.text, token.user.userId),
          token.headers.get("X-IdempotencyKey").getOrElse(""))
        val futResp = v1EmotionFuture.flatMap {
          case Some(emotionDetectionResult) =>
            logger.info(s"End of Detecting emotion for note ${note.id}")
            updateNoteAndEmotionRecordInDbWithDetectionResult(token.user.userId, note, emotionDetectionResult).map { emotionRecord =>
              Ok(Json.toJson(emotionRecord))
            }
          case None =>
            logger.info(s"End of Detecting emotion for note ${note.id}")
            Future.successful(Accepted(Json.obj("message" -> "Emotion detection result not available")))
        }

        futResp.recover {
          case e: Exception =>
            logger.error("Failed to detect emotion", e)
            InternalServerError(Json.obj("message" -> "Failed to detect emotion"))
        }
      }
    )
  }

  private def updateNoteAndEmotionRecordInDbWithDetectionResult(userId: Long, note: Note,
                                                                emotionDetectionResult: EmotionDetectionResult
                                                               ): Future[EmotionRecord] = {
    val emotionRecordId = note.emotionRecordId.getOrElse(throw new Exception("Missing emotionRecordId"))
    note.id.getOrElse(throw new Exception("Missing noteId"))

    for {
      _ <- emotionRecordService.updateWithEmotionDetectionResult(userId, emotionRecordId, emotionDetectionResult)
      _ <- noteService.update(copyEmotionDetectionResultToNote(note.copy(userId = Some(userId)), emotionDetectionResult))
      _ <- noteService.addTodosFromAi(emotionDetectionResult.todos, Some(userId), note.emotionRecordId, note.id)
      emotionRecord <- emotionRecordService.findByIdForUser(emotionRecordId, userId).map {
        case Some(emotionRecord) =>
          emotionRecord
        case None =>
          logger.error("Failed to find emotion record {}", value("emotionRecordId", emotionRecordId))
          throw new Exception(s"Failed to find emotion record $emotionRecordId")
      }
    } yield emotionRecord
  }


  def acceptTodo(noteTodoId: Long): Action[AnyContent] =
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

  private def copyEmotionDetectionResultToNote(note: Note, emotionDetectionResult: EmotionDetectionResult) = {
    note.
      copy(
        suggestion = emotionDetectionResult.suggestion,
        description = emotionDetectionResult.description,
        todos = emotionDetectionResult.todos,
        title = emotionDetectionResult.textTitle
      )
  }
}