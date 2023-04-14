package service

import com.google.inject.ImplementedBy
import controllers.model.EmotionData
import dao.{DatabaseExecutionContext, EmotionDao, SubEmotionDao, SuggestedActionDao, TriggerDao}

import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@ImplementedBy(classOf[EmotionDataServiceImpl])
trait EmotionDataService {
  def fetchEmotionData(): Future[EmotionData]
}

class EmotionDataServiceImpl @Inject()(emotionDao: EmotionDao,
                                   subEmotionDao: SubEmotionDao,
                                   triggerDao: TriggerDao,
                                   suggestedActionDao: SuggestedActionDao,
                                   databaseExecutionContext: DatabaseExecutionContext
                                  ) extends EmotionDataService {
  override def fetchEmotionData(): Future[EmotionData] = { // TODO: add caching
    Future {
      databaseExecutionContext.withConnection { implicit connection =>
        val emotions = emotionDao.findAll()
        val subEmotionsWithSuggestedActions = subEmotionDao.findAll().map(subEmotion => controllers.model.
          SubEmotionWithActions(subEmotion, suggestedActionDao.findAllBySubEmotionId(subEmotion.subEmotionId.getOrElse(
            throw new RuntimeException("subEmotionId is null")))))
        val triggers = triggerDao.findAll()
        val emotionSubEmotions = emotions.map(emotion => controllers.model.EmotionWithSubEmotions(emotion,
          subEmotionsWithSuggestedActions.filter(_.subEmotion.parentEmotionId.
            getOrElse(throw new RuntimeException("EmotionId is null")) == emotion.id)))
        EmotionData(emotionSubEmotions, triggers)
      }
    }
  }
}