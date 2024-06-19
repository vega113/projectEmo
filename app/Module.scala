import com.google.inject.AbstractModule
import com.google.inject.name.Names.named
import liquibase.LiquibaseRunner
import play.api.libs.concurrent.AkkaGuiceSupport
import service.ai.{ChatGptEmotionDetectionServiceImpl, EmoDetectionServiceWithAssistantImpl, EmotionDetectionService}

class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bind(classOf[LiquibaseRunner]).asEagerSingleton()
    bind(classOf[ShutdownHook]).asEagerSingleton()
    bind(classOf[EmotionDetectionService]).annotatedWith(named("ChatGpt")).
      to(classOf[ChatGptEmotionDetectionServiceImpl])
    bind(classOf[EmotionDetectionService]).annotatedWith(named("ChatGptAssistant")).
      to(classOf[EmoDetectionServiceWithAssistantImpl])
  }

}
