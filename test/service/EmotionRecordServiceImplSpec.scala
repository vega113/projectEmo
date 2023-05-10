package service

import controllers.model._
import org.scalatestplus.play._
import org.scalatestplus.mockito.MockitoSugar
import dao.model._
import dao.{DatabaseExecutionContext, EmotionRecordDao}
import org.mockito.Mockito._
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

import scala.concurrent.{Await, Future}
import uitil.TestObjectsFactory.createEmotionRecord

class EmotionRecordServiceImplSpec extends PlaySpec with MockitoSugar {
  trait TestData {
    val mockEmotionRecordDao = mock[EmotionRecordDao]
    val mockEmotionDataService = mock[EmotionDataService]
    val connection = mock[java.sql.Connection]
    val fakeDatabaseExecutionContext = new DatabaseExecutionContext {
      override def withConnection[A](block: java.sql.Connection => A): A = {
        block(connection)
      }
    }

    val emotionRecord = createEmotionRecord()
    val suggestions: List[SuggestedAction] = List(
      SuggestedAction(Some("Amusement"), "Go for a walk"),
      SuggestedAction(Some("Amusement"), "Go for a run"),
      SuggestedAction(Some("Amusement"), "Go for a swim")
    )
    val emotionData = EmotionData(
      List(
        EmotionTypesWithEmotions(
          "Positive",
          List(
            EmotionWithSubEmotions(
              Emotion(Some("Joy"), Some("Joy"), Some("Positive"), Some("description")),
              List(
                SubEmotionWithActions(
                  SubEmotion(Some("Amusement"), Some("Amusement"), Some("description"), Some("Joy")),
                  suggestions
                )
              )
            )
          )
        )
      ),
      List()
    )
  }

  "EmotionRecordServiceImpl" should {
    "parse emotion data cache into map successfully" in new TestData {
      val emotionRecordServiceImpl = new EmotionRecordServiceImpl(mockEmotionRecordDao, mockEmotionDataService, fakeDatabaseExecutionContext)
      val actual: List[SuggestedAction] = emotionRecordServiceImpl.parseEmotionCacheIntoSuggestions(emotionData).values.flatten.toList
      actual mustEqual suggestions
    }

    "find suggestions by EmotionRecord successfully" in new TestData {

      val emotionRecordServiceImpl = new EmotionRecordServiceImpl(mockEmotionRecordDao, mockEmotionDataService, fakeDatabaseExecutionContext)


      when(mockEmotionDataService.fetchEmotionData()).thenReturn(Future.successful(emotionData))

      val expectedResult = suggestions

      val actual = Await.result(emotionRecordServiceImpl.findSuggestionsByEmotionRecord(emotionRecord), 10000.seconds)

      actual mustEqual expectedResult
    }
  }
}
