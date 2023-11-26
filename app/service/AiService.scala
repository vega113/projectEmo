package service

import com.google.inject.ImplementedBy
import dao.model.AiDbObj
import dao.{AiDao, DatabaseExecutionContext}

import javax.inject.Inject

@ImplementedBy(classOf[AiServiceImpl])
trait AiService {
  def insert(aiResponse: AiDbObj, responseId: String): Option[Long]
}

class AiServiceImpl @Inject() (databaseExecutionContext: DatabaseExecutionContext, aiDao: AiDao) extends AiService {

  private lazy val logger = play.api.Logger(getClass)
  override def insert(aiResponse: AiDbObj, responseId: String): Option[Long] = {
    logger.info( s"inserting AiResponse: $aiResponse")
    databaseExecutionContext.withConnection({ implicit connection =>
      aiDao.insert(aiResponse)
    })
    None
  }
}
