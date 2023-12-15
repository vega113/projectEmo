package service.ai

import com.google.inject.ImplementedBy
import dao.{AiAssistant, DatabaseExecutionContext}
import dao.ai.AiDao
import dao.model.AiDbObj
import play.api.libs.json.{JsValue, Json, Reads}
import service.model.AiThread

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[AiDbServiceImpl])
trait AiDbService {
  def saveAiThread(aiThread: AiThread): Future[Option[Long]]

  def deleteAiAssistantByExternalId(externalId: String): Future[Boolean]

  def saveAiResponseAsync(userId: Long, response: JsValue): Future[Option[Long]]
  def saveAiAssistantAsync(aiAssistant: AiAssistant): Future[Option[Long]]

  def fetchAiAssistantByExternalId(externalId: String): Future[Option[AiAssistant]]

  def fetchDefaultAiAssistantForType(assistantType: String): Future[Option[AiAssistant]]

  def fetchThreadByExternalId(externalId: String): Future[Option[AiThread]]

  def fetchThreadById(id: Long): Future[Option[AiThread]]

  def fetchThreadByUserIdAndType(userId: Long, assistantType: String): Future[Option[AiThread]]
}

class AiDbServiceImpl @Inject()(databaseExecutionContext: DatabaseExecutionContext, aiDao: AiDao) extends AiDbService {

  private lazy val logger = play.api.Logger(getClass)
  private def insert(aiResponse: AiDbObj): Option[Long] = {
    logger.info( s"inserting AiResponse: $aiResponse")
    databaseExecutionContext.withConnection({ implicit connection =>
      aiDao.insert(aiResponse)
    })
    None
  }

  override def saveAiResponseAsync(userId: Long, response: JsValue): Future[Option[Long]] = {
    val idFut: Future[Option[Long]] = Future(insert(
      AiDbObj(None, Json.stringify(response), userId, None))
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
}
