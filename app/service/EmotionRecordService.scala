package service

import com.google.inject.{ImplementedBy, Inject}
import controllers.model
import dao.model._
import dao.{DatabaseExecutionContext, EmotionRecordDao}

import java.time.{Instant, LocalDate}
import scala.collection.immutable.ListMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.math.Ordered.orderingToOrdered

@ImplementedBy(classOf[EmotionRecordServiceImpl])
trait EmotionRecordService {
  def findAllByUserIdAndDateRange(userId: Long, startDate: String, endDate: String): Future[List[EmotionRecord]]

  def findAll(): Future[List[EmotionRecord]]

  def findByIdForUser(recordId: Long, userId: Long): Future[Option[EmotionRecord]]

  def findAllByUserId(userId: Long): Future[List[EmotionRecord]]

  def insert(emotionRecord: EmotionRecord): Future[Option[Long]]

  def update(emotionRecord: EmotionRecord): Future[Int]

  def delete(id: Long): Future[Int]

  def findSuggestionsByEmotionRecord(record: EmotionRecord): Future[List[SuggestedAction]]

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
                                          emotionDataService: EmotionDataService,
                                          databaseExecutionContext: DatabaseExecutionContext
                                        ) extends EmotionRecordService {
  def emotionRecordsToDoughnutTriggerChartData(records: List[EmotionRecord]): List[DoughnutChartData] = {
    val recordsByTrigger: Map[String, List[EmotionRecord]] = records.groupBy(record => {
      record.triggers match {
        case Nil => "Empty"
        case triggers => triggers.head.triggerName.getOrElse("undefined")
      }
    })
    val triggersDoughnutChartData = recordsByTrigger.map { case (triggerName, recordsForTrigger) =>
      val intensitySum = recordsForTrigger.map(_.intensity).sum
      DoughnutChartData(triggerName, recordsForTrigger.length, intensitySum, Color.fromName(triggerName).map(_.value))
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
      DoughnutChartData(emotionType, recordsForType.length, intensitySum, Color.fromName(emotionType).map(_.value))
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
    val emotionTypes: List[String] = List("Positive", "Negative", "Neutral")
    val triggerTypes:List[String] = List("People", "Places", "Situations", "Other", "Empty")
    LineChartTrendDataSet(
      rows = generateLineChartTrendDataRowsForEmotionTypesTriggers(days),
      emotionTypes = emotionTypes,
      triggerTypes = triggerTypes,
      colors = Color.toMap,
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


  def findSuggestionsByEmotionRecord(record: EmotionRecord): Future[List[SuggestedAction]] = {
    emotionDataService.fetchEmotionData().map(emotionData => {
      val subEmotionIdToSuggestedActionMap: ListMap[String, List[SuggestedAction]] = parseEmotionCacheIntoSuggestions(emotionData)
      val out: List[SuggestedAction] = record.subEmotions.flatMap(subEmotion => {
        subEmotionIdToSuggestedActionMap.get(subEmotion.subEmotionId.getOrElse("no name")).toList.flatten
      })
      out
    })
  }

  private[service] def parseEmotionCacheIntoSuggestions(emotionData: model.EmotionData) = {
    val listOfPairs: List[(String, List[SuggestedAction])] = for {
      emotionType <- emotionData.emotionTypes
      emotion <- emotionType.emotions
      subEmotion <- emotion.subEmotions
    } yield(subEmotion.subEmotion.subEmotionId.getOrElse("no name"), subEmotion.suggestedActions)
    ListMap(listOfPairs: _*)
  }

  private def preProcessEmotionRecord(emotionRecord: EmotionRecord): EmotionRecord = {
    emotionRecord.copy(notes = emotionRecord.notes.map(note => {
      note.copy(title = Option(noteService.makeTitle(note.text)))
    }), tags = (emotionRecord.tags.toSet ++ noteService.extractTags(emotionRecord.notes.map(_.text).mkString(" "))).
      toList)
  }

  override def insert(emotionRecord: EmotionRecord): Future[Option[Long]] = {
    Future.successful(databaseExecutionContext.withConnection({ implicit connection =>
      val processedEmotionRecord = preProcessEmotionRecord(emotionRecord)
      emotionRecordDao.insert(processedEmotionRecord)
    }))
  }

  override def update(emotionRecord: EmotionRecord): Future[Int] = {
    Future.successful(databaseExecutionContext.withConnection({ implicit connection =>
      emotionRecordDao.update(emotionRecord)
    }))
  }

  override def delete(id: Long): Future[Int] = {
    Future.successful(databaseExecutionContext.withConnection({ implicit connection =>
      emotionRecordDao.delete(id)
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
    name match {
      case "Positive" => Some("#3f51b5")
      case "Negative" => Some("#ffb74d")
      case "Neutral" => Some("#e57373")
      case _ => Some("#D3D3D3")
    }
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

