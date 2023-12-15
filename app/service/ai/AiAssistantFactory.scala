package service.ai

import com.google.inject.{ImplementedBy, Inject}
import service.UserInfoService

@ImplementedBy(classOf[AiAssistantFactoryImpl])
trait AiAssistantFactory {
  def fetchOrCreateAiAssistant(userId: Long, assistantType: Option[String]): AiAssistantService
}

class AiAssistantFactoryImpl @Inject() (aiDbService: AiDbService, userInfoService: UserInfoService,
                                        apiService: AiAssistantApiService) extends AiAssistantFactory {
  override def fetchOrCreateAiAssistant(userId: Long, assistantType: Option[String]): AiAssistantService = {
    assistantType match {
      case _ => new ChatGptAiAssistantServiceImpl(aiDbService, userInfoService, apiService)
    }
  }
}