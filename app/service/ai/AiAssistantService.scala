package service.ai

import com.google.inject.Inject
import dao.AiAssistant
import service.UserInfoService
import service.model.{AiMessage, AiThread, ThreadRun}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait AiAssistantService {
  def fetchAssistantForUser(userId: Long, aiAssistantType: String): Future[AiAssistant]

  def createOrFetchThread(userId: Long, aiAssistant: AiAssistant, threadType: String): Future[AiThread]

  def fetchAllMessagesForThread(externalThreadId: String): Future[List[AiMessage]]
  def fetchLastMessageByAssistantForThreadOlderThan(questionMessage: AiMessage): Future[AiMessage]

  def addMessageToThread(externalThreadId: String, message: String): Future[AiMessage]

  def makeRunInstructionsForUser(userId: Long): Future[Option[String]]

  def runAssistant(externalThreadId: String, aiAssistantId: String, instructions: Option[String]): Future[ThreadRun]

  def pollThreadRunUntilComplete(externalThreadId: String, threadRunId: String): Future[ThreadRun]

}

class ChatGptAiAssistantServiceImpl @Inject() (aiDbService: AiDbService, userInfoService: UserInfoService) extends AiAssistantService {

  private lazy val logger = play.api.Logger(getClass)

  override def fetchAssistantForUser(userId: Long, aiAssistantType: String): Future[AiAssistant] = {
    logger.info(s"Fetching assistant for user $userId")
    aiDbService.fetchDefaultAiAssistantForType(aiAssistantType).flatMap {
      case Some(aiAssistant) => Future.successful(aiAssistant)
      case None =>
        Future.failed(new Exception(s"Could not find default assistant for type $aiAssistantType"))
    }
  }

  def createThreadForUser(userId: Long, threadType: String): AiThread = {
    logger.info(s"Creating thread for user $userId")
    val aiThread = ??? // call API to create thread createThreadResponse case class 
    aiDbService.saveAiThreadAsync(aiThread)
    aiThread
  }

  override def createOrFetchThread(userId: Long, aiAssistant: AiAssistant, threadType: String): Future[AiThread] = {
    logger.info(s"Creating or fetching thread for user $userId")
    fetchThreadForUser(userId).flatMap {
      case Some(thread) => Future.successful(thread)
      case _ =>
        createThreadForUser(userId, threadType)
        // update user info with thread id
        // return thread
        ??? // TODO
    }
  }

  private def fetchThreadForUser(userId: Long): Future[Option[AiThread]] = {
    userInfoService.fetchUserInfo(userId).map {
      case Some(userInfo) => userInfo.threadId
      case None => throw new Exception(s"Could not find user info for user $userId")
    }.flatMap {
      case Some(threadId) => aiDbService.fetchThreadById(threadId)
      case None => Future.failed(new Exception(s"Could not find thread for user $userId"))
    }
  }

  override def fetchAllMessagesForThread(externalThreadId: String): Future[List[AiMessage]] = ???

  override def fetchLastMessageByAssistantForThreadOlderThan(questionMessage: AiMessage): Future[AiMessage] = ???

  override def addMessageToThread(externalThreadId: String, message: String): Future[AiMessage] = ???

  override def makeRunInstructionsForUser(userId: Long): Future[Option[String]] = ???

  override def runAssistant(externalThreadId: String, aiAssistantId: String, instructions: Option[String]): Future[ThreadRun] = ???

  override def pollThreadRunUntilComplete(externalThreadId: String, threadRunId: String): Future[ThreadRun] = ???
}
