package service.ai

import com.google.inject.ImplementedBy
import dao.DatabaseExecutionContext
import dao.ai.AiDao
import dao.model.{AiAssistant, AiDbObj, EmotionDetectionResult}
import play.api.libs.json.{JsValue, Json, Reads}
import service.model.AiThread

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

@ImplementedBy(classOf[AiDbServiceImpl])
trait AiDbService {
  def saveAiThread(aiThread: AiThread): Future[Option[Long]]

  def deleteAiAssistantByExternalId(externalId: String): Future[Boolean]

  def saveAiResponse(userId: Long, response: JsValue,
                     originalText: Option[String] = None,
                     tag: Option[String] = None,
                     elapsedTime: Option[Double] = None,
                     idempotenceKey: Option[String] = None,
                    ): Future[Option[Long]]
  def saveAiAssistantAsync(aiAssistant: AiAssistant): Future[Option[Long]]

  def fetchAiAssistantByExternalId(externalId: String): Future[Option[AiAssistant]]

  def fetchDefaultAiAssistantForType(assistantType: String): Future[Option[AiAssistant]]

  def fetchThreadByExternalId(externalId: String): Future[Option[AiThread]]

  def fetchThreadById(id: Long): Future[Option[AiThread]]

  def fetchThreadByUserIdAndType(userId: Long, assistantType: String): Future[Option[AiThread]]

  def fetchAiResponseByRequestId(requestId: String): Future[AiDbObj]
}

class AiDbServiceImpl @Inject()(databaseExecutionContext: DatabaseExecutionContext, aiDao: AiDao) extends AiDbService {

  private lazy val logger = play.api.Logger(getClass)
  private def insert(aiResponse: AiDbObj): Option[Long] = {
    logger.info( s"inserting AiResponse for userId: ${aiResponse.userId}")
    Try {
      databaseExecutionContext.withConnection({ implicit connection =>
        aiDao.insert(aiResponse)
      })
    } match {
      case scala.util.Success(Some(id: Long)) =>
        Option(id)
      case _ => {
        logger.error(s"Failed to insert AiResponse for userId: ${aiResponse.userId}")
        None
      }
      case scala.util.Failure(e) => {
        logger.error(s"Failed to insert AiResponse for userId: ${aiResponse.userId}", e)
        None
      }
    }
  }

  override def saveAiResponse(userId: Long, response: JsValue,
                              originalText: Option[String] = None,
                              tag: Option[String] = None,
                              elapsedTime: Option[Double] = None,
                              idempotenceKey: Option[String] = None,
                             ): Future[Option[Long]] = {
    val idFut: Future[Option[Long]] = Future(insert(
      AiDbObj(None, Json.stringify(response), userId, originalText, tag, elapsedTime, None, idempotenceKey))
    )
    idFut.onComplete {
      case scala.util.Success(_) => logger.info(s"Successfully saved AI response for user: $userId")
      case scala.util.Failure(e) => logger.error(s"Failed to save AI response: for user $userId", e)
    }
    idFut
  }

  override def saveAiAssistantAsync(aiAssistant: AiAssistant): Future[Option[Long]] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      Future.successful(aiDao.insertAiAssistant(aiAssistant))
    })
  }

  override def deleteAiAssistantByExternalId(externalId: String): Future[Boolean] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      Future.successful(aiDao.deleteAiAssistantByExternalId(externalId))
    })
  }

  override def fetchAiAssistantByExternalId(externalId: String): Future[Option[AiAssistant]] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      Future.successful(aiDao.fetchAiAssistantByExternalId(externalId))
    })
  }

  override def fetchDefaultAiAssistantForType(assistantType: String): Future[Option[AiAssistant]] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      Future.successful(aiDao.fetchDefaultAiAssistantForType(assistantType))
    })
  }

  override def fetchThreadByExternalId(externalId: String): Future[Option[AiThread]] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      Future.successful(aiDao.fetchThreadByExternalId(externalId))
    })
  }

  override def fetchThreadByUserIdAndType(userId: Long, assistantType: String): Future[Option[AiThread]] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      Future.successful(aiDao.fetchThreadByUserIdAndType(userId, assistantType))
    })
  }

  override def fetchThreadById(id: Long): Future[Option[AiThread]] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      Future.successful(aiDao.fetchThreadById(id))
    })
  }

  override def saveAiThread(aiThread: AiThread): Future[Option[Long]] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      Future.successful(aiDao.insertAiThread(aiThread))
    })
  }

  override def fetchAiResponseByRequestId(requestId: String): Future[AiDbObj] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      Future.successful(aiDao.fetchAiResponseByRequestId(requestId))
    })
  }
}
