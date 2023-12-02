package service.ai

import service.model.{AiAssistant, AiMessage, AiThread, ChatGptCreateAssistantRequest, ThreadRun}

import java.time.{LocalDateTime, ZoneOffset}
import scala.concurrent.Future

trait AiAssistantService {
  def fetchAssistantForUser(userId: Long, aiAssistantType: String): Future[AiAssistant]

  def createOrFetchThread(userId: Long, aiAssistant: AiAssistant, threadType: String): Future[AiThread]
  def fetchThreadForUser(userId: Long): Future[AiThread]

  def fetchAllMessagesForThread(externalThreadId: String): Future[List[AiMessage]]
  def fetchLastMessageByAssistantForThreadOlderThan(questionMessage: AiMessage): Future[AiMessage]

  def addMessageToThread(externalThreadId: String, message: String): Future[AiMessage]

  def makeRunInstructionsForUser(userId: Long): Future[Option[String]]

  def runAssistant(externalThreadId: String, aiAssistantId: String, instructions: Option[String]): Future[ThreadRun]

  def pollThreadRunUntilComplete(externalThreadId: String, threadRunId: String): Future[ThreadRun]

}

class ChatGptAiAssistantServiceImpl extends AiAssistantService {

  private lazy val logger = play.api.Logger(getClass)

  override def fetchAssistantForUser(userId: Long, aiAssistantType: String): Future[AiAssistant] = {
    logger.info(s"Fetching assistant for user $userId")
    Future.successful(AiAssistant(
      id = Some(1),
      externalId = "1",
      name = "1",
      description = None,
      isDefault = false,
      created = LocalDateTime.now(),
      lastUpdated = None,
      createdAtProvider = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli(),
      assistantType = None
    ))
  }

  override def createOrFetchThread(userId: Long, aiAssistant: AiAssistant, threadType: String): Future[AiThread] = ???

  override def fetchThreadForUser(userId: Long): Future[AiThread] = ???

  override def fetchAllMessagesForThread(externalThreadId: String): Future[List[AiMessage]] = ???

  override def fetchLastMessageByAssistantForThreadOlderThan(questionMessage: AiMessage): Future[AiMessage] = ???

  override def addMessageToThread(externalThreadId: String, message: String): Future[AiMessage] = ???

  override def makeRunInstructionsForUser(userId: Long): Future[Option[String]] = ???

  override def runAssistant(externalThreadId: String, aiAssistantId: String, instructions: Option[String]): Future[ThreadRun] = ???

  override def pollThreadRunUntilComplete(externalThreadId: String, threadRunId: String): Future[ThreadRun] = ???
}
