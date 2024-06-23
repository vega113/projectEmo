package service

import com.google.inject.ImplementedBy
import dao.model.TranscribedText
import io.github.sashirestela.openai.SimpleOpenAI
import io.github.sashirestela.openai.domain.audio.TranscriptionRequest.TimestampGranularity
import io.github.sashirestela.openai.domain.audio.{AudioResponseFormat, TranscriptionRequest}
import play.api.Configuration
import util.RichCompletableFuture._

import java.net.http.HttpClient
import java.nio.file.Path
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[OpenAiWhisperServiceImpl])
trait TranscriberService {
  def transcribeAudioToText(path: Path): Future[TranscribedText]
}

class OpenAiWhisperServiceImpl @Inject() (config: Configuration) extends TranscriberService {

  private val executionContext: ExecutionContext = ExecutionContext.global
  private val executorService = executionContext.asInstanceOf[java.util.concurrent.ExecutorService]

  override def transcribeAudioToText(path: Path): Future[TranscribedText] = {

    val duration = java.time.Duration.ofSeconds(config.get[Duration]("openai.timeout").toSeconds.toInt)
    val httpClient = HttpClient.newBuilder()
      .connectTimeout(duration)
      .executor(executorService)
      .build()
    val openAi = SimpleOpenAI.builder()
      .apiKey(config.get[String]("openai.apikey"))
      .httpClient(httpClient)
      .build()


    val audioRequest = TranscriptionRequest.builder.file(path).model("whisper-1").
      responseFormat(AudioResponseFormat.VERBOSE_JSON).temperature(0.2).timestampGranularity(TimestampGranularity.WORD).
      timestampGranularity(TimestampGranularity.SEGMENT).build

    openAi.audios.transcribe(audioRequest).asScala.map(response => {
      TranscribedText(response.getText)
    })
  }
}
