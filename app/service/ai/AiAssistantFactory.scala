package service.ai

import akka.actor.ActorSystem
import com.google.inject.{ImplementedBy, Inject}
import play.api.Configuration
import service.UserInfoService

@ImplementedBy(classOf[AiAssistantFactoryImpl])
trait AiAssistantFactory {
  def fetchOrCreateAiAssistant(userId: Long, assistantType: Option[String]): AiAssistantService
}

class AiAssistantFactoryImpl @Inject() (aiDbService: AiDbService, userInfoService: UserInfoService,
                                        apiService: AiAssistantApiService,
                                        config: Configuration,
                                        system: ActorSystem) extends AiAssistantFactory {
  override def fetchOrCreateAiAssistant(userId: Long, assistantType: Option[String]): AiAssistantService = {
    assistantType match {
      case _ => new ChatGptAiAssistantServiceImpl(aiDbService, userInfoService, apiService, config, system)
    }
  }
}
