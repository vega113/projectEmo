package service.ai

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.RestartSettings
import akka.stream.scaladsl.Source
import com.google.inject.{ImplementedBy, Inject}
import dao.AiAssistant
import play.api.Configuration
import service.UserInfoService
import service.ai.ChatGptModel._
import service.model.{AiMessage, AiThread}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[ChatGptAiAssistantServiceImpl])
trait AiAssistantService {
  def fetchAssistantForUser(userId: Long, aiAssistantType: String): Future[AiAssistant]

  def createOrFetchThread(userId: Long, aiAssistant: AiAssistant, threadType: String): Future[AiThread]

  def fetchLastMessageByAssistantForThreadOlderThan(questionMessage: AiMessage): Future[AiMessage]

  def addMessageToThread(externalThreadId: String, message: String): Future[AiMessage]

  def makeRunInstructionsForUser(userId: Long): Future[Option[String]]

  def runThread(externalThreadId: String, aiAssistantId: String, instructions: Option[String]): Future[ChatGptThreadRunResponse
  ]

  def pollThreadRunUntilComplete(externalThreadId: String, threadRunId: String): Future[ChatGptThreadRunResponse]

}

class ChatGptAiAssistantServiceImpl @Inject()(aiDbService: AiDbService, userInfoService: UserInfoService,
                                              apiService: AiAssistantApiService,
                                              config: Configuration,
                                              system: ActorSystem) extends AiAssistantService {

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
    val createThreadResponse = apiService.makeApiPostCall[ChatGptCreateThreadResponse](path)
    createThreadResponse.onComplete {
      case scala.util.Success(value) => logger.info(s"Successfully created thread for user $userId, response: $value")
      case scala.util.Failure(exception) => logger.error(s"Failed to create thread for user $userId", exception)
    }
    createThreadResponse
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
      case Some(userInfo) =>
        logger.info(s"Found user info for user $userId: $userInfo")
        userInfo.threadId
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

  override def fetchLastMessageByAssistantForThreadOlderThan(questionMessage: AiMessage): Future[AiMessage] = {
    logger.info(s"Fetching last message by assistant for thread ${questionMessage.externalThreadId}")
    val path: String = s"/v1/threads/${questionMessage.externalThreadId}/messages?limit=1"
    val response = apiService.makeApiGetCall[ChatGptResponseMessages](path)

    response.onComplete {
      case scala.util.Success(value) =>
        logger.info(s"Successfully fetched last message by assistant for thread" +
        s" ${questionMessage.externalThreadId}")
      case scala.util.Failure(exception) => logger.error(s"Failed to fetch last message by assistant for thread" +
        s" ${questionMessage.externalThreadId}", exception)
    }
    response.map(_.data.head.toAiMessage)
  }

  override def addMessageToThread(externalThreadId: String, message: String): Future[AiMessage] = {
    logger.info(s"Adding message to thread $externalThreadId")
    val path: String = s"/v1/threads/$externalThreadId/messages"
    val body = ChatGptAddMessageRequest(
      role = "user",
      content = message, None, None)
    val resp = apiService.makeApiPostCall[ChatGptAddMessageRequest, ChatGptMessageResponse](path, body).map { response =>
      response.toAiMessage
    }
    resp.onComplete({
      case scala.util.Success(value) => logger.info(
        s"Successfully added message to thread $externalThreadId, response: $value")
      case scala.util.Failure(exception) => logger.error(s"Failed to add message to thread $externalThreadId", exception)
    })
    resp
  }

  override def makeRunInstructionsForUser(userId: Long): Future[Option[String]] = {
    val perRunInstructions = None
    logger.info(s"Making per run instructions for user $userId V2")
    Future.successful(perRunInstructions)
  }

  override def runThread(externalThreadId: String, aiAssistantId: String, instructions: Option[String]): Future[ChatGptThreadRunResponse] = {
    logger.info(s"Running assistant for thread $externalThreadId")
    val path: String = s"/v1/threads/$externalThreadId/runs"
    val body = ChatGptThreadRunRequest(
      assistant_id = aiAssistantId,
      instructions = instructions,
      model = config.getOptional[String]("openai.model")
    )
    val response = apiService.makeApiPostCall[ChatGptThreadRunRequest, ChatGptThreadRunResponse](path, body)
    response.onComplete {
      case scala.util.Success(value) => logger.info(s"Successfully ran assistant for thread $externalThreadId," +
        s" response: $value")
      case scala.util.Failure(exception) => logger.error(s"Failed to run assistant for thread $externalThreadId", exception)
    }
    response
  }

  import akka.actor.ActorSystem
  import akka.stream.scaladsl.{RestartSource, Sink}
  import scala.concurrent.duration._

  import akka.actor.ActorSystem
  import akka.stream.scaladsl.{RestartSource, Sink, Source}
  import akka.stream.RestartSettings
  import scala.concurrent.duration._

  override def pollThreadRunUntilComplete(externalThreadId: String, threadRunId: String): Future[ChatGptThreadRunResponse] = {
    val initialInterval = 5.seconds
    val initialCount = 6
    val minBackoff = 5.seconds
    val maxBackoff = 30.seconds
    val randomFactor = 0.2

    implicit val actorSystem: ActorSystem = system

    val settings = RestartSettings(minBackoff, maxBackoff, randomFactor)

    val initialSource = Source.tick(initialInterval, initialInterval, NotUsed).take(initialCount).mapAsync(1) { _ =>
      val path: String = s"/v1/threads/$externalThreadId/runs/$threadRunId"
      apiService.makeApiGetCall[ChatGptThreadRunResponse](path)
    }

    val backoffSource = RestartSource.withBackoff(settings) { () =>
      Source.future {
        val path: String = s"/v1/threads/$externalThreadId/runs/$threadRunId"
        apiService.makeApiGetCall[ChatGptThreadRunResponse](path)
      }
    }

    val source = initialSource.concat(backoffSource)

    source.takeWhile(out => {
      logger.info(s"Polling thread run for thread $externalThreadId, runId: $threadRunId, status: ${out.status}")
      out.status match {
        case "completed" => false
        case _ => true
      }
    }, inclusive = false).runWith(Sink.last)
  }
}
