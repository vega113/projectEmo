package service

import com.google.inject.ImplementedBy
import dao.model.SubEmotion
import dao.{DatabaseExecutionContext, SubEmotionDao}

import javax.inject.Inject
import scala.concurrent.Future

@ImplementedBy(classOf[SubEmotionServiceImpl])
trait SubEmotionService {
  def findAllByEmotionId(id: String): Future[List[SubEmotion]]
}
class SubEmotionServiceImpl @Inject()(
                                    subEmotionDao: SubEmotionDao,
                                   databaseExecutionContext: DatabaseExecutionContext) extends SubEmotionService {
  def findAllByEmotionId(id: String): Future[List[SubEmotion]] = {
    Future.successful(databaseExecutionContext.withConnection { implicit connection =>
      subEmotionDao.findAllByEmotionId(id)
    })
  }
}
