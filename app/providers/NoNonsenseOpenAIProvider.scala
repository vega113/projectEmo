package providers

import akka.actor.ActorSystem
import akka.stream.Materializer
import io.cequence.openaiscala.service.ws.Timeouts
import io.cequence.openaiscala.service.{OpenAIService, OpenAIServiceFactory}
import play.api.Configuration

import javax.inject.{Inject, Provider}
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

class NoNonsenseOpenAIProvider @Inject()(config: Configuration) extends Provider[OpenAIService]{

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  implicit val materializer: Materializer = Materializer(ActorSystem())

  override def get(): OpenAIService = {

    val timeout = config.get[Option[Duration]]("openai.timeout").map(_.toMillis.toInt)
    OpenAIServiceFactory(
      apiKey = config.get[String]("openai.apikey"),
      timeouts = Option(Timeouts(
        requestTimeout = timeout
      ))
    )
  }
}
