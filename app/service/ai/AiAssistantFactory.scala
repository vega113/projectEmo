package service.ai

import com.google.inject.ImplementedBy

@ImplementedBy(classOf[AiAssistantFactoryImpl])
trait AiAssistantFactory {
  def fetchOrCreateAiAssistant(userId: Long, assistantType: Option[String]): AiAssistantService
}

class AiAssistantFactoryImpl extends AiAssistantFactory {
  override def fetchOrCreateAiAssistant(userId: Long, assistantType: Option[String]): AiAssistantService = {
    assistantType match {
      case _ => new ChatGptAiAssistantServiceImpl()
    }
  }
}
