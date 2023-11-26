package service

import com.google.inject.{ImplementedBy, Inject}
import dao.model._
import dao.{DatabaseExecutionContext, EmotionRecordDao}
import service.enums.{ColorType, EmotionType, TriggerType}

import java.time.{Instant, LocalDate}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.sequence
import scala.math.Ordered.orderingToOrdered
import scala.util.Success

@ImplementedBy(classOf[EmotionRecordServiceImpl])
trait EmotionRecordService {
  def findAllByUserIdAndDateRange(userId: Long, startDate: String, endDate: String): Future[List[EmotionRecord]]

  def findAll(): Future[List[EmotionRecord]]

  def findByIdForUser(recordId: Long, userId: Long): Future[Option[EmotionRecord]]

  def findAllByUserId(userId: Long): Future[List[EmotionRecord]]

  def insert(emotionRecord: EmotionRecord): Future[Option[Long]]

  def update(emotionRecord: EmotionRecord): Future[Int]

  def delete(id: Long, userId: Long): Future[Boolean]

  def findEmotionRecordIdByUserIdNoteId(userId: Long, noteId: Long): Future[Option[Long]]

  def findEmotionRecordIdByUserIdTagId(userId: Long, id: Long): Future[Option[Long]]

  def groupRecordsByDate(records: List[EmotionRecord]): List[EmotionRecordDay]

  def fetchRecordsForMonthByDate(userId: Long, startDateTime: Instant, endDateTime: Instant): Future[List[EmotionRecord]]

  def emotionRecordsToSunburstChartData(records: List[EmotionRecord]): List[SunburstData]

  def emotionRecordsToDoughnutEmotionTypeTriggerChartData(records: List[EmotionRecord]): DoughnutEmotionTypesTriggersChartData
  def generateLineChartTrendDataSetForEmotionTypesTriggers(days: List[EmotionRecordDay]): LineChartTrendDataSet


}

class EmotionRecordServiceImpl @Inject()(
                                          emotionRecordDao: EmotionRecordDao,
                                          noteService: NoteService,
                                          tagService: TagService,
                                          triggerService: TriggerService,
                                          titleService: TitleService,
                                          databaseExecutionContext: DatabaseExecutionContext
                                        ) extends EmotionRecordService {

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)
  def emotionRecordsToDoughnutTriggerChartData(records: List[EmotionRecord]): List[DoughnutChartData] = {
    val recordsByTrigger: Map[String, List[EmotionRecord]] = records.groupBy(record => {
      record.triggers match {
        case Nil => "Empty"
        case triggers => triggers.head.triggerName.getOrElse("undefined")
      }
    })
    val triggersDoughnutChartData = recordsByTrigger.map { case (triggerName, recordsForTrigger) =>
      val intensitySum = recordsForTrigger.map(_.intensity).sum
      DoughnutChartData(triggerName, recordsForTrigger.length, intensitySum, ColorType.fromName(triggerName).map(_.value))
    }.toList
    triggersDoughnutChartData.sortWith((d1, d2) => {
      val order = List("People", "Places", "Situations", "Other", "Empty")
      order.indexOf(d1.name) < order.indexOf(d2.name)
    })
  }

  def emotionRecordsToDoughnutEmotionTypeChartData(records: List[EmotionRecord]):List[DoughnutChartData] = {
    val recordsByType: Map[String, List[EmotionRecord]] = records.groupBy(_.emotionType)
    val chartData = recordsByType.map { case (emotionType, recordsForType) =>
      val intensitySum = recordsForType.map(_.intensity).sum
      DoughnutChartData(emotionType, recordsForType.length, intensitySum, ColorType.fromName(emotionType).map(_.value))
    }.toList
    chartData.sortWith((d1, d2) => {
      val order = List("Positive", "Negative", "Neutral")
      order.indexOf(d1.name) < order.indexOf(d2.name)
    })
    chartData
  }

  override def findAll(): Future[List[EmotionRecord]] = {
    Future.successful(databaseExecutionContext.withConnection { implicit connection =>
      emotionRecordDao.findAll()
    })
  }

  override def findByIdForUser(recordId: Long, userId: Long): Future[Option[EmotionRecord]]= {
    Future.successful(databaseExecutionContext.withConnection({ implicit connection =>
      emotionRecordDao.findByIdForUser(recordId, userId)
    }))
  }


  override def findAllByUserId(userId: Long): Future[List[EmotionRecord]] = {
    Future.successful(databaseExecutionContext.withConnection({ implicit connection =>
      emotionRecordDao.findAllByUserId(userId).sortWith((d1, d2) => {
        val earliestTime = LocalDate.of(2022, 1, 1).atStartOfDay()
        val d1Time = d1.created.getOrElse(earliestTime)
        val d2Time = d2.created.getOrElse(earliestTime)
        d1Time > d2Time
      })
    }))
  }

  def groupRecordsByDate(records: List[EmotionRecord]): List[EmotionRecordDay] = {
    val recordsByDate: Map[LocalDate, List[EmotionRecord]] = records.groupBy(record => {
      record.created.map(_.toLocalDate).getOrElse(LocalDate.of(2022, 1, 1))
    })
    val out: List[EmotionRecordDay] = recordsByDate.map(entry => {
      EmotionRecordDay(entry._1, entry._2)
    }).toList.sortWith((d1, d2) => {
      d1.date > d2.date
    })
    out
  }

  def generateLineChartTrendDataSetForEmotionTypesTriggers(days: List[EmotionRecordDay]): LineChartTrendDataSet = {
    val emotionTypes: List[String] = EmotionType.toList
    val triggerTypes:List[String] = TriggerType.toList
    LineChartTrendDataSet(
      rows = generateLineChartTrendDataRowsForEmotionTypesTriggers(days),
      emotionTypes = emotionTypes,
      triggerTypes = triggerTypes,
      colors = ColorType.toMap,
    )

  }
  private[service] def generateLineChartTrendDataRowsForEmotionTypesTriggers(days: List[EmotionRecordDay]): List[LineChartTrendDataRow] = {
    def extractData[T](records: List[EmotionRecord], groupByCriteria: EmotionRecord => T): Map[T, LineChartData] = {
      records.groupBy(groupByCriteria).map(entry => {
        val intensitySum = entry._2.map(_.intensity).sum
        val recordsCount = entry._2.length
        entry._1 -> LineChartData(recordsCount, intensitySum)
      })
    }

    for {
      day <- days
    } yield {
      val emotionTypes: Map[String, LineChartData] = extractData(day.records, _.emotionType)
      val triggers: Map[String, LineChartData] = extractData(day.records, _.triggers.headOption.flatMap(_.triggerName).
        getOrElse("Empty"))
      LineChartTrendDataRow(day.date, emotionTypes, triggers)
    }
  }

  private def preProcessEmotionRecord(emotionRecord: EmotionRecord): EmotionRecord = {
    emotionRecord.copy(notes = emotionRecord.notes.map(note => {
      note.copy(title = Option(titleService.makeTitle(note.text)))
    }), tags = (emotionRecord.tags.toSet ++ noteService.extractTags(emotionRecord.notes.map(_.text).mkString(" "))).
      toList)
  }

   def insert2(emotionRecord: EmotionRecord): Future[Option[Long]] = {
    Future.successful(databaseExecutionContext.withConnection({ implicit connection =>
      val processedEmotionRecord = preProcessEmotionRecord(emotionRecord)
      emotionRecordDao.insert(processedEmotionRecord)
    }))
  }

  override def insert(emotionRecord: EmotionRecord): Future[Option[Long]] = {
    Future.successful(databaseExecutionContext.withConnection({ implicit connection =>
      val emotionRecordsIdOpt = emotionRecordDao.insert2(emotionRecord)
      emotionRecordsIdOpt
    })).andThen({
      case Success(Some(emotionRecordId)) =>
        sequence(emotionRecord.notes.map(note => {
          val title = note.title.getOrElse(titleService.makeTitle(note.text))
          noteService.insert(emotionRecordId, note.copy(title = Some(title)))
        }))

        tagService.insert(emotionRecordId, emotionRecord.tags.toSet)

        emotionRecord.triggers.foreach {
          case Trigger(Some(triggerId), _, _, _, _, _) =>
            triggerService.linkTriggerToEmotionRecord(triggerId, emotionRecordId)
          case trigger =>
            logger.error(s"Empty trigger id while inserting trigger: ${trigger.triggerId}")
        }

    })
  }

  override def update(emotionRecord: EmotionRecord): Future[Int] = {
    Future.successful(databaseExecutionContext.withConnection({ implicit connection =>
      emotionRecordDao.update(emotionRecord)
    }))
  }

  override def delete(id: Long, userId: Long): Future[Boolean] = {
    Future(databaseExecutionContext.withConnection({ implicit connection =>
      emotionRecordDao.deleteByIdAndUserId(id, userId)
    }))
  }

  def findAllByUserIdAndDateRange(userId: Long, startDate: String, endDate: String): Future[List[EmotionRecord]] = {
    Future.successful(databaseExecutionContext.withConnection({ implicit connection =>
      emotionRecordDao.findAllByUserIdAndDateRange(userId, startDate, endDate)
    }))
  }

  def fetchRecordsForMonthByDate(userId: Long, startDateTime: Instant, endDateTime: Instant): Future[List[EmotionRecord]] = {
    Future.successful(databaseExecutionContext.withConnection({ implicit connection =>
      val records: List[EmotionRecord] = emotionRecordDao.findAllByUserIdAndDateRange(
        userId, startDateTime.toString, endDateTime.toString)
      records
    }))
  }

  private def computeColor(name: String): Option[String] = {
    ColorType.fromName(name).map(_.value)
  }

  def emotionRecordsToSunburstChartData(records: List[EmotionRecord]): List[SunburstData] = {
    // First grouping by emotionType
    val recordsByType: Map[String, List[EmotionRecord]] = records.groupBy(_.emotionType)

    val chartData = recordsByType.flatMap { case (emotionType, recordsForType) =>
      // Second grouping by emotionName within each emotionType
      val recordsByEmotion: Map[String, List[EmotionRecord]] = recordsForType.groupBy(record =>
        record.emotion.flatMap(_.emotionName).getOrElse("undefined"))

      val secondLevel = recordsByEmotion.flatMap { case (emotionName, recordsForEmotion) =>
        // Third grouping by subEmotionName within each emotion
        val recordsBySubEmotion = recordsForEmotion
          .flatMap(_.subEmotions.flatMap(_.subEmotionName))
          .groupBy(identity)
          .view
          .mapValues(_.length)
          .toMap
        List(SunburstData(emotionName, None, recordsBySubEmotion.map { case (subEmotionName, count) =>
          SunburstData(subEmotionName, Some(count), List())
        }.toList))
      }
      List(SunburstData(emotionType, None, secondLevel.toList))
    }.toList

    chartData.map(data => data.copy(color = computeColor(data.name)))
  }

  override def findEmotionRecordIdByUserIdNoteId(userId: Long, noteId: Long): Future[Option[Long]] =
    Future.successful(databaseExecutionContext.withConnection({ implicit connection =>
      emotionRecordDao.findEmotionRecordIdByUserIdNoteId(userId, noteId)
    }))

  override def findEmotionRecordIdByUserIdTagId(userId: Long, noteId: Long): Future[Option[Long]] =
    Future.successful(databaseExecutionContext.withConnection({ implicit connection =>
      emotionRecordDao.findEmotionRecordIdByUserIdTagId(userId, noteId)
    }))

  override def emotionRecordsToDoughnutEmotionTypeTriggerChartData(records: List[EmotionRecord]): DoughnutEmotionTypesTriggersChartData =
    DoughnutEmotionTypesTriggersChartData(
      emotionRecordsToDoughnutEmotionTypeChartData(records),
      emotionRecordsToDoughnutTriggerChartData(records)
    )
}

