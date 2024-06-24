package providers

import io.github.sashirestela.openai.SimpleOpenAI
import play.api.Configuration

import java.net.http.HttpClient
import javax.inject.{Inject, Provider}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

class SimpleOpenAIProvider @Inject() (config: Configuration) extends Provider[SimpleOpenAI] {

  private lazy val executionContext: ExecutionContext = ExecutionContext.global
  private lazy val executorService = executionContext.asInstanceOf[java.util.concurrent.ExecutorService]

  override def get(): SimpleOpenAI = {
    val duration = java.time.Duration.ofSeconds(config.get[Duration]("openai.timeout").toMillis.toInt)
    val httpClient = HttpClient.newBuilder()
      .connectTimeout(duration)
      .executor(executorService)
      .build()
    SimpleOpenAI.builder()
      .apiKey(config.get[String]("openai.apikey"))
      .httpClient(httpClient)
      .baseUrl(config.get[String]("openai.baseUrl"))
      .build()
  }
}
