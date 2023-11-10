package service

import com.google.inject.ImplementedBy
import controllers.model._
import dao.{DatabaseExecutionContext, EmotionDao, SubEmotionDao, SuggestedActionDao, TriggerDao}

import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@ImplementedBy(classOf[EmotionDataServiceImpl])
trait EmotionDataService {
  def fetchEmotionData(): Future[EmotionData]
}

class EmotionDataServiceImpl @Inject()(emotionDao: EmotionDao,
                                   triggerDao: TriggerDao,
                                   databaseExecutionContext: DatabaseExecutionContext
                                  ) extends EmotionDataService {
  override def fetchEmotionData(): Future[EmotionData] = { // TODO: add caching
    Future {
      databaseExecutionContext.withConnection { implicit connection =>
        val emotions = emotionDao.findAll()
        val triggers = triggerDao.findAll()
        val emotionSubEmotions: List[EmotionWithSubEmotions] = emotions.map(emotion =>
          controllers.model.EmotionWithSubEmotions(emotion, List()))
        val emotionTypes = emotionSubEmotions.groupBy(_.emotion.emotionType).map {
          case (Some(emotionType), emotionWithSubEmotions) => EmotionTypesWithEmotions(emotionType,
            emotionWithSubEmotions)
        }.toList
        EmotionData(emotionTypes, triggers)
      }
    }
  }
}