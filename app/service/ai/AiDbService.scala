package service.ai

import com.google.inject.ImplementedBy
import dao.DatabaseExecutionContext
import dao.ai.AiDao
import dao.model.AiDbObj
import play.api.libs.json.{JsValue, Json, Reads}
import play.api.libs.ws.WSResponse
import service.model.AiAssistant

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[AiDbServiceImpl])
trait AiDbService {
  def deleteAiAssistantByExternalId(externalId: String): Future[Boolean]

  def saveAiResponseAsync(userId: Long, response: JsValue): Future[Option[Long]]
  def saveAiAssistantAsync(aiAssistant: AiAssistant): Future[Option[Long]]
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
}
