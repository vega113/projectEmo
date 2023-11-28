package service

import controllers.model._
import dao.model._
import dao.{DatabaseExecutionContext, EmotionRecordDao}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play._
import uitil.TestObjectsFactory.createEmotionRecord

import java.sql.Connection
import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class EmotionTypeRecordServiceImplSpec extends PlaySpec with MockitoSugar {
  trait TestData {
    val mockEmotionRecordDao: EmotionRecordDao = mock[EmotionRecordDao]
    val mockEmotionDataService: EmotionDataService = mock[EmotionDataService]
    val mockNoteService: NoteService = mock[NoteService]
    val connection: Connection = mock[java.sql.Connection]
    val fakeDatabaseExecutionContext: DatabaseExecutionContext = new DatabaseExecutionContext {
      override def withConnection[A](block: java.sql.Connection => A): A = {
        block(connection)
      }
    }

    val emotionRecord: EmotionRecord = createEmotionRecord()
    val suggestions: List[SuggestedAction] = List(
      SuggestedAction(Some("Amusement"), "Go for a walk"),
      SuggestedAction(Some("Amusement"), "Go for a run"),
      SuggestedAction(Some("Amusement"), "Go for a swim")
    )
    val emotionData: EmotionData = EmotionData(
      List(
        EmotionTypesWithEmotions(
          "Positive",
          List(
            EmotionWithSubEmotions(
              Emotion(Some("Joy"), Some("Joy"), Some("Positive"), Some("description")),
              List(
                SubEmotionWrapper(
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

    val records: List[EmotionRecord] = List(
      EmotionRecord(
        id = Some(1),
        emotionType = "Positive",
        userId = Some(1),
        emotion = Some(Emotion(Some("Joy"), Some("Joy"), Some("Positive"), Some("description"))),
        intensity = 2,
        subEmotions = List(SubEmotion(Some("Amusement"), Some("Amusement"), Some("description"), Some("Joy"))),
        triggers = List(),
        notes = List(),
        tags = List(),
        created = createMockDate1()
      ),
      EmotionRecord(
        id = Some(2),
        emotionType = "Positive",
        userId = Some(1),
        emotion = Some(Emotion(Some("Joy"), Some("Joy"), Some("Positive"), Some("description"))),
        intensity = 4,
        subEmotions = List(SubEmotion(Some("Amusement"), Some("Amusement"), Some("description"), Some("Joy"))),
        triggers = List(Trigger(Some(1), Some("Situations"), Some(1), Some(1), Some("description"))),
        notes = List(),
        tags = List(),
        created = createMockDate1()
      ),
      EmotionRecord(
        id = Some(3),
        emotionType = "Positive",
        userId = Some(1),
        emotion = Some(Emotion(Some("Joy"), Some("Joy"), Some("Positive"), Some("description"))),
        intensity = 3,
        subEmotions = List(SubEmotion(Some("Excitement"), Some("Excitement"), Some("description"), Some("Joy"))),
        triggers = List(Trigger(Some(1), Some("People"), Some(1), Some(1), Some("description"))),
        notes = List(),
        tags = List(),
        created = createMockDate2()
      ),
      EmotionRecord(
        id = Some(4),
        emotionType = "Neutral",
        userId = Some(1),
        emotion = Some(Emotion(Some("Other"), Some("Other"), Some("Neutral"), Some("description"))),
        intensity = 3,
        subEmotions = List(SubEmotion(Some("Indifference"), Some("Indifference"), Some("description"), Some("Other"))),
        triggers = List(Trigger(Some(1), Some("Other"), Some(1), Some(1), Some("description"))),
        notes = List(),
        tags = List(),
        created = createMockDate2()
      ),
      EmotionRecord(
        id = Some(4),
        emotionType = "Negative",
        userId = Some(1),
        emotion = Some(Emotion(Some("Anger"), Some("Anger"), Some("Negative"), Some("description"))),
        intensity = 3,
        subEmotions = List(SubEmotion(Some("Annoyance"), Some("Annoyance"), Some("description"), Some("Anger"))),
        triggers = List(Trigger(Some(1), Some("Other"), Some(1), Some(1), Some("description"))),
        notes = List(Note(None, title = Some("Note title"), text = "Note text", description = Some("description"))),
        tags = List(Tag(Some(1), "Tag name")),
        created = createMockDate2()
      ),
      EmotionRecord(
        id = Some(4),
        emotionType = "Negative",
        userId = Some(1),
        emotion = None,
        intensity = 3,
        subEmotions = List(),
        triggers = List(Trigger(Some(1), Some("People"), Some(1), Some(1), Some("description"))),
        notes = List(Note(None, title = Some("Note title"), text = "Note text", description = Some("description"))),
        tags = List(Tag(Some(1), "Tag name")),
        created = createMockDate2()
      )
    )
  }

  "EmotionRecordServiceImpl" should {
    "convert emotion records to Sunburst chart data" in new TestData {
      val mockTagService: TagService = mock[TagService]
      val mockTriggerService: TriggerService = mock[TriggerService]
      val mockTitleService: TitleService = mock[TitleService]
      val emotionRecordServiceImpl = new EmotionRecordServiceImpl(mockEmotionRecordDao, mockNoteService,
         mockTagService, mockTriggerService, mockTitleService, fakeDatabaseExecutionContext)
      // Execute the method
      private val result = emotionRecordServiceImpl.emotionRecordsToSunburstChartData(records).sortWith(_.name < _.name)

      // Verify the result
      result mustEqual
        List(SunburstData("Negative", None, List(SunburstData("undefined", None, List(), None),
          SunburstData("Anger", None, List(SunburstData("Annoyance", Some(1), List(), None)), None)), Some("#e57373")),
          SunburstData("Neutral", None, List(SunburstData("Other", None, List(SunburstData("Indifference", Some(1), List(), None)), None)), Some("#ffb74d")),
          SunburstData("Positive", None, List(SunburstData("Joy", None,
            List(SunburstData("Amusement", Some(2), List(), None),
              SunburstData("Excitement", Some(1), List(), None)), None)), Some("#3f51b5")))
    }
  }

  "convert emotion records to doughnut emotions type chart data" in new TestData {
    val mockTagService: TagService = mock[TagService]
    val mockTriggerService: TriggerService = mock[TriggerService]
    val mockTitleService: TitleService = mock[TitleService]
    val emotionRecordServiceImpl = new EmotionRecordServiceImpl(mockEmotionRecordDao, mockNoteService,
      mockTagService, mockTriggerService, mockTitleService, fakeDatabaseExecutionContext)
    // Execute the method
    private val result = emotionRecordServiceImpl.emotionRecordsToDoughnutEmotionTypeChartData(records)

    // Verify the result
    result mustEqual
      List(DoughnutChartData("Neutral", 1, 3, Some("#ffb74d")), DoughnutChartData("Negative", 2, 6, Some("#e57373")),
        DoughnutChartData("Positive", 3, 9, Some("#3f51b5")))
  }

  "convert emotion records to doughnut triggers chart data" in new TestData {
    val mockTagService: TagService = mock[TagService]
    val mockTriggerService: TriggerService = mock[TriggerService]
    val mockTitleService: TitleService = mock[TitleService]
    val emotionRecordServiceImpl = new EmotionRecordServiceImpl(mockEmotionRecordDao, mockNoteService,
      mockTagService, mockTriggerService, mockTitleService, fakeDatabaseExecutionContext)

    // add more emotion records to the list one for each trigger type
    val recordsWithMoreTriggers: List[EmotionRecord] = records ++ List(EmotionRecord(
      id = Some(1),
      emotionType = "Positive",
      userId = Some(1),
      emotion = Some(Emotion(Some("Joy"), Some("Joy"), Some("Positive"), Some("description"))),
      intensity = 5,
      subEmotions = List(SubEmotion(Some("Amusement"), Some("Amusement"), Some("description"), Some("Joy"))),
      triggers = List(Trigger(Some(1), Some("Places"), Some(1), Some(1), Some("description"))),
      notes = List(),
      tags = List(),
      created = None
    ))

    // Execute the method
    private val result = emotionRecordServiceImpl.emotionRecordsToDoughnutTriggerChartData(recordsWithMoreTriggers)

    // Verify the result
    result mustEqual
      List(DoughnutChartData("People", 2, 6, Some("#FF6B6B")),
        DoughnutChartData("Places", 1, 5, Some("#4ECDC4")),
        DoughnutChartData("Situations", 1, 4, Some("#FFD166")),
        DoughnutChartData("Other", 2, 6, Some("#839788")),
        DoughnutChartData("Empty", 1, 2, Some("#D3D3D3")))
  }

  "convert emotion records to trend line chart data" in new TestData {
    val mockTagService: TagService = mock[TagService]
    val mockTriggerService: TriggerService = mock[TriggerService]
    val mockTitleService: TitleService = mock[TitleService]
    val emotionRecordServiceImpl = new EmotionRecordServiceImpl(mockEmotionRecordDao, mockNoteService,
      mockTagService, mockTriggerService, mockTitleService, fakeDatabaseExecutionContext)

    // add more emotion records to the list one for each trigger type
    val recordsWithMoreTriggers: List[EmotionRecord] = records ++ List(EmotionRecord(
      id = Some(1),
      emotionType = "Positive",
      userId = Some(1),
      emotion = Some(Emotion(Some("Joy"), Some("Joy"), Some("Positive"), Some("description"))),
      intensity = 5,
      subEmotions = List(SubEmotion(Some("Amusement"), Some("Amusement"), Some("description"), Some("Joy"))),
      triggers = List(Trigger(Some(1), Some("Places"), Some(1), Some(1), Some("description"))),
      notes = List(),
      tags = List(),
      created = createMockDate2()
    ))

    // Execute the method
    private val result = emotionRecordServiceImpl.generateLineChartTrendDataRowsForEmotionTypesTriggers(
      emotionRecordServiceImpl.groupRecordsByDate(recordsWithMoreTriggers))

    // Verify the result
    result mustEqual List(
      LineChartTrendDataRow(LocalDate.parse("2021-01-02"),
        Map("Neutral" -> LineChartData(1, 3), "Negative" -> LineChartData(2, 6), "Positive" -> LineChartData(2, 8)),
        Map("Other" -> LineChartData(2, 6), "People" -> LineChartData(2, 6), "Places" -> LineChartData(1, 5))),
      LineChartTrendDataRow(LocalDate.parse("2021-01-01"),
        Map("Positive" -> LineChartData(2, 6)),
        Map("Empty" -> LineChartData(1, 2), "Situations" -> LineChartData(1, 4)))
    )
  }

  "check fetch data" in new TestData {
    val mockTagService: TagService = mock[TagService]
    val mockTriggerService: TriggerService = mock[TriggerService]
    val mockTitleService: TitleService = mock[TitleService]
    val emotionRecordServiceImpl = new EmotionRecordServiceImpl(mockEmotionRecordDao, mockNoteService,
      mockTagService, mockTriggerService, mockTitleService, fakeDatabaseExecutionContext)
    when(mockEmotionRecordDao.findAllByUserId(1)(connection)).thenReturn(records)


    private val actual: List[EmotionRecord] = Await.result(emotionRecordServiceImpl.findAllByUserId(1) , Duration("5s"))
    actual mustEqual List(EmotionRecord(Some(3), "Positive", Some(1), Some(Emotion(Some("Joy"), Some("Joy"), Some("Positive"), Some("description"))), 3, List(SubEmotion(Some("Excitement"), Some("Excitement"), Some("description"), Some("Joy"))), List(Trigger(Some(1), Some("People"), Some(1), Some(1), Some("description"), None)), List(), List(), None, None, Some(LocalDateTime.parse("2021-01-02T00:00"))), EmotionRecord(Some(4), "Neutral", Some(1), Some(Emotion(Some("Other"), Some("Other"), Some("Neutral"), Some("description"))), 3, List(SubEmotion(Some("Indifference"), Some("Indifference"), Some("description"), Some("Other"))), List(Trigger(Some(1), Some("Other"), Some(1), Some(1), Some("description"), None)), List(), List(), None, None, Some(LocalDateTime.parse("2021-01-02T00:00"))), EmotionRecord(Some(4), "Negative", Some(1), Some(Emotion(Some("Anger"), Some("Anger"), Some("Negative"), Some("description"))), 3, List(SubEmotion(Some("Annoyance"), Some("Annoyance"), Some("description"), Some("Anger"))), List(Trigger(Some(1), Some("Other"), Some(1), Some(1), Some("description"), None)), List(Note(None, Some("Note title"), "Note text", Some("description"), None, None, None, None, None, None)), List(Tag(Some(1), "Tag name", None)), None, None, Some(LocalDateTime.parse("2021-01-02T00:00"))), EmotionRecord(Some(4), "Negative", Some(1), None, 3, List(), List(Trigger(Some(1), Some("People"), Some(1), Some(1), Some("description"), None)), List(Note(None, Some("Note title"), "Note text", Some("description"), None, None, None, None, None, None)), List(Tag(Some(1), "Tag name", None)), None, None, Some(LocalDateTime.parse("2021-01-02T00:00"))), EmotionRecord(Some(1), "Positive", Some(1), Some(Emotion(Some("Joy"), Some("Joy"), Some("Positive"), Some("description"))), 2, List(SubEmotion(Some("Amusement"), Some("Amusement"), Some("description"), Some("Joy"))), List(), List(), List(), None, None, Some(LocalDateTime.parse("2021-01-01T00:00"))), EmotionRecord(Some(2), "Positive", Some(1), Some(Emotion(Some("Joy"), Some("Joy"), Some("Positive"), Some("description"))), 4, List(SubEmotion(Some("Amusement"), Some("Amusement"), Some("description"), Some("Joy"))), List(Trigger(Some(1), Some("Situations"), Some(1), Some(1), Some("description"), None)), List(), List(), None, None, Some(LocalDateTime.parse("2021-01-01T00:00"))))
  }

  def createMockDate1(): Option[LocalDateTime] = {
    Some(LocalDateTime.of(2021, 1, 1, 0, 0))
  }

  def createMockDate2(): Option[LocalDateTime] = {
    Some(LocalDateTime.of(2021, 1, 2, 0, 0))
  }
}
