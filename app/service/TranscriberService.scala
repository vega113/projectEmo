package service

import com.google.inject.ImplementedBy
import dao.model.TranscribedText
import io.cequence.openaiscala.service.OpenAIService
import io.cequence.openaiscala.service.OpenAIServiceFactory.DefaultSettings

import java.nio.file.Path
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[OpenAiWhisperServiceImpl])
trait TranscriberService {
  def transcribeAudioToText(path: Path): Future[TranscribedText]
}

class OpenAiWhisperServiceImpl @Inject() (openAi: OpenAIService) extends TranscriberService {

  override def transcribeAudioToText(path: Path): Future[TranscribedText] = {
    openAi.createAudioTranscription(path.toFile,
      settings = DefaultSettings.CreateTranscription.copy(language = None)).map(response => {
      TranscribedText(response.text)
    })
  }
}
