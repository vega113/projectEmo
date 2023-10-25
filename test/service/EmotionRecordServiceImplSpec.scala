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
    val mockNoteService = mock[NoteService]
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
      val emotionRecordServiceImpl = new EmotionRecordServiceImpl(mockEmotionRecordDao, mockNoteService, mockEmotionDataService, fakeDatabaseExecutionContext)
      val actual: List[SuggestedAction] = emotionRecordServiceImpl.parseEmotionCacheIntoSuggestions(emotionData).values.flatten.toList
      actual mustEqual suggestions
    }

    "find suggestions by EmotionRecord successfully" in new TestData {

      val emotionRecordServiceImpl = new EmotionRecordServiceImpl(mockEmotionRecordDao, mockNoteService, mockEmotionDataService, fakeDatabaseExecutionContext)


      when(mockEmotionDataService.fetchEmotionData()).thenReturn(Future.successful(emotionData))

      val expectedResult = suggestions

      val actual = Await.result(emotionRecordServiceImpl.findSuggestionsByEmotionRecord(emotionRecord), 10000.seconds)

      actual mustEqual expectedResult
    }
  }


  "EmotionRecordServiceImpl" should {
    "convert emotion records to chart data" in new TestData {
      // Prepare test data
      val records = List(
        EmotionRecord(
          id = Some(1),
          emotionType = "Positive",
          userId = Some(1),
          emotion = Some(Emotion(Some("Joy"), Some("Joy"), Some("Positive"), Some("description"))),
          intensity = 5,
          subEmotions = List(SubEmotion(Some("Amusement"), Some("Amusement"), Some("description"), Some("Joy"))),
          triggers = List(),
          notes = List(),
          tags = List(),
          created = None
        ),
        EmotionRecord(
          id = Some(2),
          emotionType = "Positive",
          userId = Some(1),
          emotion = Some(Emotion(Some("Joy"), Some("Joy"), Some("Positive"), Some("description"))),
          intensity = 8,
          subEmotions = List(SubEmotion(Some("Amusement"), Some("Amusement"), Some("description"), Some("Joy"))),
          triggers = List(),
          notes = List(),
          tags = List(),
          created = None
        ),
        EmotionRecord(
          id = Some(3),
          emotionType = "Positive",
          userId = Some(1),
          emotion = Some(Emotion(Some("Joy"), Some("Joy"), Some("Positive"), Some("description"))),
          intensity = 7,
          subEmotions = List(SubEmotion(Some("Excitement"), Some("Excitement"), Some("description"), Some("Joy"))),
          triggers = List(),
          notes = List(),
          tags = List(),
          created = None
        ),
        EmotionRecord(
          id = Some(4),
          emotionType = "Neutral",
          userId = Some(1),
          emotion = Some(Emotion(Some("Other"), Some("Other"), Some("Neutral"), Some("description"))),
          intensity = 3,
          subEmotions = List(SubEmotion(Some("Indifference"), Some("Indifference"), Some("description"), Some("Other"))),
          triggers = List(),
          notes = List(),
          tags = List(),
          created = None
        )
      )

      val emotionRecordServiceImpl = new EmotionRecordServiceImpl(mockEmotionRecordDao, mockNoteService, mockEmotionDataService, fakeDatabaseExecutionContext)
      // Execute the method
      private val result = emotionRecordServiceImpl.emotionRecordsToChartData(records)

      // Verify the result
      result mustEqual Map(
        "Positive" -> Map(
          "Joy" -> Map(
            "Amusement" -> 2,
            "Excitement" -> 1
          )
        ),
        "Neutral" -> Map(
          "Other" -> Map(
            "Indifference" -> 1
          )
        )
      )
    }
  }
}
