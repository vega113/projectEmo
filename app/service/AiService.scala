package service

import com.google.inject.ImplementedBy
import dao.model.AiDbObj
import dao.{AiDao, DatabaseExecutionContext}

import javax.inject.Inject

@ImplementedBy(classOf[AiServiceImpl])
trait AiService {
  def insert(aiResponse: AiDbObj): Option[Long]
}

class AiServiceImpl @Inject() (databaseExecutionContext: DatabaseExecutionContext, aiDao: AiDao) extends AiService {

  private lazy val logger = play.api.Logger(getClass)
  override def insert(aiResponse: AiDbObj): Option[Long] = {
    logger.info( s"insertAiResponse: $aiResponse")
    databaseExecutionContext.withConnection({ implicit connection =>
      aiDao.insert(aiResponse)
    })
    None
  }
}
