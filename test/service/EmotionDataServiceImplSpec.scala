package service

import scala.concurrent.{Await, Future}
import org.scalatestplus.play._
import org.scalatestplus.mockito.MockitoSugar
import controllers.model.{EmotionData, EmotionTypesWithEmotions, EmotionWithSubEmotions, SubEmotionWrapper}
import dao.model.{Emotion, SubEmotion, SuggestedAction, Trigger}
import dao.{DatabaseExecutionContext, EmotionDao, SubEmotionDao, SuggestedActionDao, TriggerDao}
import org.mockito.Mockito.when
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import play.api.libs.json.Json

class EmotionDataServiceImplSpec extends PlaySpec with MockitoSugar {
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

      val emotionServiceImpl = new EmotionDataServiceImpl(mockEmotionDao, mockTriggerDao,
         fakeDatabaseExecutionContext, mockSubEmotionDao)

      val emotions = List(Emotion(Some("Joy"), Option("Joy"), Some("Positive")), Emotion(Some("Sadness"), Option("Sadness"), Some("Negative")))
      val subEmotions = List(SubEmotion(Some("Content"), Some("Content"), Some("description"), Some("Joy")))
      val triggers = List(Trigger(Some(1), Some("Bad weather"), None, None, Some("Bad weather")))

      when(mockEmotionDao.findAll()(connection)).thenReturn(emotions)
      when(mockSubEmotionDao.findAll()(connection)).thenReturn(subEmotions)
      when(mockTriggerDao.findAll()(connection)).thenReturn(triggers)

      val subEmotionWithActions: SubEmotionWrapper = SubEmotionWrapper(
        subEmotions.head, List())
      val emotionWithSubEmotions1: EmotionWithSubEmotions = EmotionWithSubEmotions(
        emotions.head,List(subEmotionWithActions)
      )
      val emotionWithSubEmotions2: EmotionWithSubEmotions = EmotionWithSubEmotions(
        emotions.last, List()
      )
      val emotionType1 = EmotionTypesWithEmotions("Positive", List(emotionWithSubEmotions1))
      val emotionType2 = EmotionTypesWithEmotions("Negative", List(emotionWithSubEmotions2))
      val expectedEmotionData = EmotionData(List(emotionType2, emotionType1),triggers)

      val actual: EmotionData = Await.result(emotionServiceImpl.fetchEmotionData(), 10000.seconds)
      val sortedActual = EmotionData(actual.emotionTypes.sortBy(_.emotionType), actual.triggers)
      println("actual: " + Json.toJson(sortedActual))
      println("expected: " + Json.toJson(expectedEmotionData))

      sortedActual mustEqual expectedEmotionData
    }
  }
}

