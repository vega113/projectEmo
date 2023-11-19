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
    for {
      emotions <- Future(databaseExecutionContext.withConnection(implicit connection => emotionDao.findAll()))
      triggers <- Future(databaseExecutionContext.withConnection(implicit connection => triggerDao.findAll()))
      subEmotions <- Future(databaseExecutionContext.withConnection(implicit connection => subEmotionDao.findAll()))
    } yield {
      val emotionSubEmotions: List[EmotionWithSubEmotions] = emotions.map(emotion =>
        EmotionWithSubEmotions(emotion,
          subEmotions.filter(subEmotion => equals(subEmotion.parentEmotionId, emotion.id)).
            map(subEmotion => SubEmotionWrapper(subEmotion, List.empty))
        )
      )
      val emotionTypes = emotionSubEmotions.groupBy(_.emotion.emotionType).map {
        case (Some(emotionType), emotionWithSubEmotions) => EmotionTypesWithEmotions(emotionType,
          emotionWithSubEmotions)
      }.toList
      EmotionData(emotionTypes, triggers)
    }
  }

  private def equals[T](a: Option[T], b: Option[T]): Boolean = (a, b) match {
    case (Some(a), Some(b)) => a == b
    case _ => false
  }
}