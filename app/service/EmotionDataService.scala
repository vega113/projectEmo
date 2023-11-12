package service

import com.google.inject.ImplementedBy
import controllers.model._
import dao.model.{Emotion, SubEmotion}
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
                                   databaseExecutionContext: DatabaseExecutionContext,
                                    subEmotionDao: SubEmotionDao,
                                  ) extends EmotionDataService {
  override def fetchEmotionData(): Future[EmotionData] = { // TODO: add caching
    Future {
      databaseExecutionContext.withConnection { implicit connection =>
        val emotions: List[Emotion] = emotionDao.findAll()
        val triggers = triggerDao.findAll()

        val subEmotions: List[SubEmotion] = subEmotionDao.findAll()

        val emotionSubEmotions: List[EmotionWithSubEmotions] = emotions.map(emotion =>
          controllers.model.EmotionWithSubEmotions(emotion, subEmotions.filter(_.parentEmotionId == emotion.id)))
        val emotionTypes = emotionSubEmotions.groupBy(_.emotion.emotionType).map {
          case (Some(emotionType), emotionWithSubEmotions) => EmotionTypesWithEmotions(emotionType,
            emotionWithSubEmotions)
        }.toList
        EmotionData(emotionTypes, triggers)
      }
    }
  }
}