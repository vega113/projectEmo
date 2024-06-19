package controllers

import auth.AuthenticatedAction
import dao.model.TranscribedText
import play.api.libs.Files
import play.api.libs.Files.TemporaryFile
import play.api.libs.json.Json
import play.api.mvc.{Action, ControllerComponents, MultipartFormData}
import service.TranscriberService

import java.nio.file.Paths
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future



class TranscriberController @Inject()(cc: ControllerComponents,
                               authenticatedAction: AuthenticatedAction, transcriberService: TranscriberService)
  extends EmoBaseController(cc, authenticatedAction) {

  private lazy val logger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  def transcribeAudioToText(): Action[MultipartFormData[Files.TemporaryFile]] = Action(parse.multipartFormData) andThen authenticatedAction async { implicit request =>
    request.body.file("audio").map { audio =>
      logger.info(s"Transcribing audio file size: ${audio.fileSize}")
      val ref: TemporaryFile = audio.ref
      val tempFilePath = ref.path
      val newFilePath = Paths.get(tempFilePath.toString + ".webm")
      import java.nio.file.{Files, Paths}
      Files.move(tempFilePath, newFilePath)
      transcriberService.transcribeAudioToText(newFilePath).map(transcribedText => {
        Ok(Json.toJson(transcribedText))
      })
    }.getOrElse {
      Future.successful(BadRequest("Missing file"))
    }
  }
}
