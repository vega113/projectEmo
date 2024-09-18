package service.ai

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.RestartSettings
import akka.stream.scaladsl.{RestartSource, Sink, Source}
import com.google.inject.{ImplementedBy, Inject}
import dao.model.{AiAssistant, UserInfo}
import io.cequence.openaiscala.domain.{AssistantId, AssistantToolOutput, ModelId, Pagination, Run, RunStatus, SortOrder, Thread, ThreadFullMessage}
import io.cequence.openaiscala.service.OpenAIService
import io.cequence.openaiscala.service.OpenAIServiceFactory.DefaultSettings
import play.api.Configuration
import service.UserInfoService
import service.model.AiThread

import java.time.{LocalDateTime, ZoneOffset}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

@ImplementedBy(classOf[ChatGptAiAssistantServiceImpl])
trait AiAssistantService {
  def fetchAssistantForUser(userId: Long, aiAssistantType: String): Future[String]

  def createOrFetchThreadId(userId: Long, aiAssistantIs: String, threadType: String): Future[String]

  def fetchLastMessageByAssistantForThreadOlderThan(questionMessage: ThreadFullMessage): Future[ThreadFullMessage]

  def addMessageToThread(externalThreadId: String, message: String): Future[ThreadFullMessage]

  def makeRunInstructionsForUser(userId: Long): Future[Option[String]]

  def runThread(externalThreadId: String, aiAssistantId: String, instructions: Option[String]): Future[Run]

  def submitToolOutput(runWithFunctionCall: Run): Future[Unit]


  def pollThreadRunUntilNeededStatus(externalThreadId: String, threadRunId: String, expectedRunStatus: RunStatus): Future[Run]

}

class ChatGptAiAssistantServiceImpl @Inject()(aiDbService: AiDbService, userInfoService: UserInfoService,
                                              config: Configuration,
                                              system: ActorSystem,
                                              openAi: OpenAIService) extends AiAssistantService with FunctionTools {

  private lazy val logger = play.api.Logger(getClass)

  override def fetchAssistantForUser(userId: Long, aiAssistantType: String): Future[String] = {
    logger.info(s"Fetching assistant for user $userId")
    userInfoService.fetchUserInfo(userId).map {
      case Some(info@UserInfo(_, _, _, assistantId, _, _, _, _, _, _)) =>
        logger.info(s"Found user info for user $userId: $info")
        Some(assistantId)
      case _ =>
        logger.info(s"Could not find user info for user $userId")
        // create default assistant
        None
    }.flatMap {
      case Some(assistantId) =>
        Future.successful(assistantId)
      case _ =>
        fetchOrCreateDefaultAssistant(aiAssistantType)
    }
  }

  private def fetchOrCreateDefaultAssistant(aiAssistantType: String) = {
    for {
      maybeAssistantId <- aiDbService.fetchDefaultAiAssistantIdForType(aiAssistantType)
      assistantId <- maybeAssistantId match {
        case Some(id) =>
          logger.info(s"Found default assistant for type $aiAssistantType: $id")
          Future.successful(id)
        case None =>
          for {
            assistant <- openAi.createAssistant(
              model = config.get[String]("openai.model"),
              name = Some("Emotions Expert"),
              instructions = config.getOptional[String]("openai.systemPromt"),
              tools = emoTools
            )
            _ = logger.info(s"Created default assistant for type $aiAssistantType: $assistant")
            _ <- aiDbService.saveAiAssistantAsync(AiAssistant(
              id = None,
              externalId = assistant.id.id,
              name = assistant.name.getOrElse(""),
              description = assistant.description,
              isDefault = true,
              created = LocalDateTime.now(),
              lastUpdated = None,
              createdAtProvider = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
              assistantType = Option(aiAssistantType)
            ))
          } yield assistant.id.id
      }
    } yield assistantId
  }

  private def createThreadForUser(userId: Long): Future[Thread] = {
    logger.info(s"Creating thread for user $userId")
    val createThreadResponse = openAi.createThread()
    createThreadResponse.onComplete {
      case scala.util.Success(value) => logger.info(s"Successfully created thread for user $userId, response: $value")
      case scala.util.Failure(exception) => logger.error(s"Failed to create thread for user $userId", exception)
    }
    createThreadResponse
  }

  override def createOrFetchThreadId(userId: Long, aiAssistantId: String, threadType: String): Future[String] = {
    logger.info(s"Creating or fetching thread for user $userId")
    fetchThreadIdForUser(userId).flatMap {
      case Some(thread) => Future.successful(thread)
      case _ =>
        val response = for {
          createThreadResponse <- createThreadForUser(userId)
          newAiThread = AiThread(
            id = None,
            externalId = createThreadResponse.id,
            userId = userId,
            threadType = threadType,
            isDeleted = false,
            created = None)
          aiThread <- aiDbService.saveAiThread(newAiThread)
          _ <- userInfoService.upsertUserInfo(userId, aiAssistantId, aiThread.externalId)
        } yield {
          aiThread.externalId
        }
        response.onComplete {
          case scala.util.Success(value) =>
            //
            logger.info(s"Successfully created thread: ${value}")
          case scala.util.Failure(exception) => logger.error(s"Failed to create thread: $exception", exception)
        }
        response
    }
  }

  private def fetchThreadIdForUser(userId: Long): Future[Option[String]] = {
    userInfoService.fetchUserInfo(userId).map {
      case Some(UserInfo(_, _, _, _, Some(threadId), _, _, _, _, _)) =>
        Some(threadId)
      case None =>
        logger.info(s"Could not find user info for user $userId")
        None
      case _ =>
        logger.info(s"Could not find thread for user $userId")
        None
    }
  }

  override def fetchLastMessageByAssistantForThreadOlderThan(questionMessage: ThreadFullMessage): Future[ThreadFullMessage] = {
    logger.info(s"Fetching last message by assistant for thread ${questionMessage.thread_id}")
    val response: Future[Seq[ThreadFullMessage]] = openAi.
      listThreadMessages(questionMessage.thread_id, Pagination.limit(1), Some(SortOrder.desc))

    response.onComplete {
      case scala.util.Success(value) =>
        logger.info(s"Successfully fetched last message by assistant for thread" +
          s" ${questionMessage.thread_id}")
      case scala.util.Failure(exception) => logger.error(s"Failed to fetch last message by assistant for thread" +
        s" ${questionMessage.thread_id}", exception)
    }
    response.map(x =>
      x.head
    )
  }

  override def addMessageToThread(externalThreadId: String, message: String): Future[ThreadFullMessage] = {
    logger.info(s"Adding message to thread $externalThreadId")
    val aiMessage: Future[ThreadFullMessage] = openAi.createThreadMessage(threadId = externalThreadId, content = message)

    aiMessage.onComplete({
      case scala.util.Success(value) => logger.info(
        s"Successfully added message to thread $externalThreadId, response: $value")
      case scala.util.Failure(exception) => logger.error(s"Failed to add message to thread $externalThreadId", exception)
    })
    aiMessage
  }

  override def makeRunInstructionsForUser(userId: Long): Future[Option[String]] = {
    val perRunInstructions = None
    logger.info(s"Making per run instructions for user $userId V2")
    Future.successful(perRunInstructions)
  }

  override def runThread(externalThreadId: String, aiAssistantId: String, instructions: Option[String]): Future[Run] = {
    logger.info(s"Running assistant for thread $externalThreadId")
    val runSettings = DefaultSettings.CreateRun.copy(model = Option(ModelId.gpt_4o))
    val runF = openAi.createRun(threadId = externalThreadId, assistantId = AssistantId(aiAssistantId), tools = emoTools,
      responseToolChoice = None, settings = runSettings, stream = false)
    runF.onComplete {
      case scala.util.Success(value) => logger.info(s"Successfully ran assistant for thread $externalThreadId," +
        s" response: $value")
      case scala.util.Failure(exception) => logger.error(s"Failed to run assistant for thread $externalThreadId", exception)
    }
    runF
  }

  override def pollThreadRunUntilNeededStatus(externalThreadId: String, threadRunId: String, expectedRunStatus: RunStatus): Future[Run] = {
    val initialInterval = 3.seconds
    val initialCount = 6
    val minBackoff = 3.seconds
    val maxBackoff = 30.seconds
    val randomFactor = 0.2

    implicit val actorSystem: ActorSystem = system

    val settings = RestartSettings(minBackoff, maxBackoff, randomFactor)

    val initialSource = Source.tick(initialInterval, initialInterval, NotUsed).take(initialCount).mapAsync(1) { _ =>
      openAi.retrieveRun(externalThreadId, threadRunId)
    }

    val backoffSource = RestartSource.withBackoff(settings) { () =>
      Source.future {
        openAi.retrieveRun(externalThreadId, threadRunId)
      }
    }

    val source = initialSource.concat(backoffSource)

    source.collect {
      case Some(run) => run
    }.takeWhile(currentRun => {
      logger.info(s"Polling thread run for thread $externalThreadId, runId: $threadRunId, status: ${currentRun.status}")
      currentRun.status != expectedRunStatus
    }, inclusive = true).runWith(Sink.last)
  }

  override def submitToolOutput(runWithFunctionCall: Run): Future[Unit] = {

    val toolCalls = runWithFunctionCall.required_action.get.submit_tool_outputs.tool_calls
    val functionCalls = toolCalls.collect {
      case toolCall if toolCall.function.name == "detect_emotion" =>
        toolCall
    }
    openAi.submitToolOutputs(runWithFunctionCall.thread_id, runWithFunctionCall.id, Option(functionCalls.map(
      toolCall => {
        logger.info(s"Submitting tool output for thread ${runWithFunctionCall.thread_id}," +
          s" runId: ${runWithFunctionCall.id}, toolCallId: ${toolCall.id}")
        AssistantToolOutput(
          output = Some("saved"),
          tool_call_id = toolCall.id
        )
      }
    ))
    ).map(_ => ())
  }
}