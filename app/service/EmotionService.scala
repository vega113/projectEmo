package service

import com.google.inject.ImplementedBy
import dao.model.Emotion
import dao.{DatabaseExecutionContext, EmotionDao}

import javax.inject.Inject
import scala.concurrent.Future

@ImplementedBy(classOf[EmotionServiceImpl])
trait EmotionService {
  def findAll(): Future[List[Emotion]]
}

class EmotionServiceImpl @Inject()(
                                    emotionDao: EmotionDao,
                                   databaseExecutionContext: DatabaseExecutionContext) extends EmotionService {
  def findAll(): Future[List[Emotion]] = {
    Future.successful(databaseExecutionContext.withConnection { implicit connection =>
      emotionDao.findAll()
    })
  }
}
