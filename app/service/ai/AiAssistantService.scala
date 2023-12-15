package service.ai

import com.google.inject.{ImplementedBy, Inject}
import dao.AiAssistant
import service.UserInfoService
import service.ai.ChatGptModel.ChatGptCreateThreadResponse
import service.model.{AiMessage, AiThread, ThreadRun}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[ChatGptAiAssistantServiceImpl])
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

class ChatGptAiAssistantServiceImpl @Inject() (aiDbService: AiDbService, userInfoService: UserInfoService,
                                               apiService: AiAssistantApiService) extends AiAssistantService {

  private lazy val logger = play.api.Logger(getClass)

  override def fetchAssistantForUser(userId: Long, aiAssistantType: String): Future[AiAssistant] = {
    logger.info(s"Fetching assistant for user $userId")
    aiDbService.fetchDefaultAiAssistantForType(aiAssistantType).flatMap {
      case Some(aiAssistant) => Future.successful(aiAssistant)
      case None =>
        Future.failed(new Exception(s"Could not find default assistant for type $aiAssistantType"))
    }
  }

  private def createThreadForUser(userId: Long): Future[ChatGptCreateThreadResponse] = {
    logger.info(s"Creating thread for user $userId")
    val path: String = "/v1/threads"
    apiService.makeApiPostCall[ChatGptCreateThreadResponse](path)
  }

  override def createOrFetchThread(userId: Long, aiAssistant: AiAssistant, threadType: String): Future[AiThread] = {
    logger.info(s"Creating or fetching thread for user $userId")
    fetchThreadForUser(userId).flatMap {
      case Some(thread) => Future.successful(thread)
      case _ =>
        val response = for {
          createThreadResponse <- createThreadForUser(userId)
          newAiThread = createThreadResponse.toAiThread(userId, threadType)
          internalThreadIdOption <- aiDbService.saveAiThread(newAiThread)
          internalThreadId <- internalThreadIdOption.fold(
            Future.failed[Long](new Exception("Failed to get internalThreadId")))(Future.successful)
          aiAssistantId <- aiAssistant.id.fold(
            Future.failed[Long](new Exception("Failed to get aiAssistant.id")))(x => Future.successful(x))
          _ <- userInfoService.upsertUserInfo(userId, aiAssistantId, internalThreadId)
        } yield {
          newAiThread.copy(id = Some(internalThreadId))
        }
        response.onComplete {
          case scala.util.Success(value) if value.id.isDefined =>
            //
            logger.info(s"Successfully created thread: ${value.id.get}")
          case scala.util.Failure(exception) => logger.error(s"Failed to create thread: $exception", exception)
          case scala.util.Success(value) => logger.error(s"Failed to create thread: $value")
        }
        response
    }
  }

  private def fetchThreadForUser(userId: Long): Future[Option[AiThread]] = {
    userInfoService.fetchUserInfo(userId).map {
      case Some(userInfo) => userInfo.threadId
      case _ =>
        logger.info(s"Could not find user info for user $userId")
        None
    }.flatMap {
      case Some(threadId) => aiDbService.fetchThreadById(threadId)
      case None =>
        logger.info(s"Could not find thread for user $userId")
        Future.successful(None)
    }
  }

  override def fetchAllMessagesForThread(externalThreadId: String): Future[List[AiMessage]] = ???

  override def fetchLastMessageByAssistantForThreadOlderThan(questionMessage: AiMessage): Future[AiMessage] = ???

  override def addMessageToThread(externalThreadId: String, message: String): Future[AiMessage] = ???

  override def makeRunInstructionsForUser(userId: Long): Future[Option[String]] = ???

  override def runAssistant(externalThreadId: String, aiAssistantId: String, instructions: Option[String]): Future[ThreadRun] = ???

  override def pollThreadRunUntilComplete(externalThreadId: String, threadRunId: String): Future[ThreadRun] = ???
}
