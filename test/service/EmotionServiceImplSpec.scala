package service

import scala.concurrent.{Await, Future}
import org.scalatestplus.play._
import org.scalatestplus.mockito.MockitoSugar
import controllers.model.{EmotionData, EmotionTypesWithEmotions, EmotionWithSubEmotions, SubEmotionWithActions}
import dao.model.{Emotion, SubEmotion, SuggestedAction, Trigger}
import dao.{DatabaseExecutionContext, EmotionDao, SubEmotionDao, SuggestedActionDao, TriggerDao}
import org.mockito.Mockito.when
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

class EmotionServiceImplSpec extends PlaySpec with MockitoSugar {
  "EmotionServiceImpl" should {
    "fetch EmotionData successfully" in {
      val mockEmotionDao = mock[EmotionDao]
      val mockSubEmotionDao = mock[SubEmotionDao]
      val mockSuggestedActionDao = mock[SuggestedActionDao]
      val mockTriggerDao = mock[TriggerDao]
      val connection = mock[java.sql.Connection]
      val fakeDatabaseExecutionContext = new DatabaseExecutionContext {
        override def withConnection[A](block: java.sql.Connection => A): A = {
          block(connection)
        }
      }

      val emotionServiceImpl = new EmotionDataServiceImpl(mockEmotionDao, mockSubEmotionDao, mockTriggerDao,
        mockSuggestedActionDao, fakeDatabaseExecutionContext)

      val emotions = List(Emotion("Joy", "Joy", "Positive"), Emotion("Sadness", "Sadness", "Negative"))
      val subEmotions = List(SubEmotion(Some("Content"), Some("Content"), Some("description"), Some("Joy")))
      val suggestedActions = List(SuggestedAction(Some("Content"), "Watch a comedy"))
      val triggers = List(Trigger(Some(1), Some("Bad weather"), None, None, Some("Bad weather")))

      when(mockEmotionDao.findAll()(connection)).thenReturn(emotions)
      when(mockSubEmotionDao.findAll()(connection)).thenReturn(subEmotions)
      when(mockSuggestedActionDao.findAllBySubEmotionId("Content")(connection)).thenReturn(suggestedActions)
      when(mockTriggerDao.findAll()(connection)).thenReturn(triggers)

      val subEmotionWithActions: SubEmotionWithActions = SubEmotionWithActions(
        subEmotions.head, suggestedActions)
      val emotionWithSubEmotions1: EmotionWithSubEmotions = EmotionWithSubEmotions(
        emotions.head,List(subEmotionWithActions)
      )
      val emotionWithSubEmotions2: EmotionWithSubEmotions = EmotionWithSubEmotions(
        emotions.last, List()
      )
      val emotionType1 = EmotionTypesWithEmotions("Positive", List(emotionWithSubEmotions1))
      val emotionType2 = EmotionTypesWithEmotions("Negative", List(emotionWithSubEmotions2))
      val expectedEmotionData = EmotionData(List(emotionType2, emotionType1),triggers)

      val actual: Future[EmotionData] = emotionServiceImpl.fetchEmotionData()

      Await.result(actual, 1.second) mustEqual expectedEmotionData
    }
  }
}

