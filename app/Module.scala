import com.google.inject.AbstractModule
import com.google.inject.name.Names.named
import io.cequence.openaiscala.service.OpenAIService
import liquibase.LiquibaseRunner
import play.api.libs.concurrent.AkkaGuiceSupport
import providers.NoNonsenseOpenAIProvider
import service.ai.{ChatGptEmotionDetectionServiceImpl, EmoDetectionServiceWithAssistantImpl, EmotionDetectionService}

import javax.inject.Singleton

class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bind(classOf[LiquibaseRunner]).asEagerSingleton()
    bind(classOf[ShutdownHook]).asEagerSingleton()

    bind(classOf[EmotionDetectionService]).annotatedWith(named("ChatGpt")).
      to(classOf[ChatGptEmotionDetectionServiceImpl]).in(classOf[Singleton])

    bind(classOf[EmotionDetectionService]).annotatedWith(named("ChatGptAssistant")).
      to(classOf[EmoDetectionServiceWithAssistantImpl]).in(classOf[Singleton])

    bind(classOf[OpenAIService]).toProvider(classOf[NoNonsenseOpenAIProvider]).in(classOf[Singleton])
  }
}
