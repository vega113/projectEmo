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

@ImplementedBy(classOf[EmotionRecordServiceImpl])
trait EmotionRecordService {
  def findAllByUserIdAndDateRange(userId: Long, startDate: String, endDate: String): Future[List[EmotionRecord]]

  def findAll(): Future[List[EmotionRecord]]

  def findByIdForUser(recordId: Long, userId: Long): Future[Option[EmotionRecord]]

  def findAllByUserId(userId: Long): Future[List[EmotionRecord]]

  def insert(emotionRecord: EmotionRecord): Future[Option[Long]]

  def update(emotionRecord: EmotionRecord): Future[EmotionRecord]

  def delete(id: Long, userId: Long): Future[Boolean]

  def groupRecordsByDate(records: List[EmotionRecord]): List[EmotionRecordDay]

  def fetchRecordsForMonthByDate(userId: Long, startDateTime: Instant, endDateTime: Instant): Future[List[EmotionRecord]]

  def emotionRecordsToSunburstChartData(records: List[EmotionRecord]): List[SunburstData]

  def emotionRecordsToDoughnutEmotionTypeTriggerChartData(records: List[EmotionRecord]): DoughnutEmotionTypesTriggersChartData

  def generateLineChartTrendDataSetForEmotionTypesTriggers(days: List[EmotionRecordDay]): LineChartTrendDataSet

  def updateWithEmotionDetectionResult(userId: Long, emotionRecordId: Long,
                                       emotionDetectionResult: EmotionDetectionResult): Future[Boolean]

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

  def emotionRecordsToDoughnutEmotionTypeChartData(records: List[EmotionRecord]): List[DoughnutChartData] = {
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

  override def findByIdForUser(recordId: Long, userId: Long): Future[Option[EmotionRecord]] = {
    Future.successful(databaseExecutionContext.withConnection({ implicit connection =>
      emotionRecordDao.findByIdForUser(recordId, userId)
    }))
  }

  override def findAllByUserId(userId: Long): Future[List[EmotionRecord]] = {
    logger.info(s"Fetching all emotion records for user: $userId")
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
    val triggerTypes: List[String] = TriggerType.toList
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

  override def insert(emotionRecord: EmotionRecord): Future[Option[Long]] = {
    Future.successful(databaseExecutionContext.withConnection({ implicit connection =>
      val emotionRecordsIdOpt = emotionRecordDao.insert(emotionRecord)


      emotionRecordsIdOpt match {
        case Some(emotionRecordId) =>
          sequence(emotionRecord.notes.map(note => {
            val title = note.title.getOrElse(titleService.makeTitle(note.text))
            noteService.insert(note.copy(title = Some(title), emotionRecordId = Some(emotionRecordId), userId = emotionRecord.userId))
          }))

          tagService.insert(emotionRecordId, emotionRecord.userId, emotionRecord.tags.toSet)

          for {
            subEmotion <- emotionRecord.subEmotions
            subEmotionId <- subEmotion.subEmotionId
          } yield {
            logger.info(s"Linking sub emotion to emotion record: $subEmotionId, $emotionRecordId")
            emotionRecordDao.linkSubEmotionToEmotionRecord(subEmotionId, emotionRecordId)
          }

          emotionRecord.triggers.foreach {
            case Trigger(Some(triggerId), _, _, _, _, _) =>
              triggerService.linkTriggerToEmotionRecord(triggerId, emotionRecordId)
            case trigger =>
              logger.error(s"Empty trigger id while inserting trigger: ${trigger.triggerId}")
          }
        case None =>
          logger.error(s"Failed to insert emotion record: $emotionRecord")
      }
      emotionRecordsIdOpt
    }))
  }

  override def update(emotionRecord: EmotionRecord): Future[EmotionRecord] = {
    for {
      fixedEmotionId <- fixMainEmotionIdBySubEmotionId(emotionRecord.subEmotionId)
      fixedEmotionType <- fixEmotionTypeByMainEmotion(fixedEmotionId)
      result <- {
        val emotionRecordFixed = emotionRecord.copy(emotionType = fixedEmotionType.getOrElse("Unknown"),
          emotionId = fixedEmotionId)
        databaseExecutionContext.withConnection({ implicit connection =>
          val count = emotionRecordDao.update(emotionRecordFixed)
          if (count > 0) {
            logger.info(s"Updated emotion record: ${emotionRecord.id}, userId ${emotionRecord.userId}, count: $count")
            Future.successful(emotionRecordDao.findByIdForUser(emotionRecord.id, emotionRecord.userId).getOrElse(
              throw new Exception(s"Failed to find emotion record after update: $emotionRecord")))
          } else {
            logger.error(s"Failed to update emotion record: $emotionRecord")
            Future.failed(new Exception(s"Failed to update emotion record: $emotionRecord"))
          }
        })
      }
    } yield result
  }

  private def fixMainEmotionIdBySubEmotionId(subEmotionId: Option[String]): Future[Option[String]] = {
    subEmotionId match {
      case Some(subEmotionId) =>
        databaseExecutionContext.withConnection({ implicit connection =>
          val mainEmotionIdOpt = emotionRecordDao.findMainEmotionIdBySubEmotionId(subEmotionId)
          mainEmotionIdOpt match {
            case Some(mainEmotionId) =>
              logger.info(s"Found main emotion id for sub emotion id: $subEmotionId, main emotion id: $mainEmotionId")
              Future.successful(Some(mainEmotionId))
            case None =>
              logger.error(s"Failed to find main emotion id for sub emotion id: $subEmotionId")
              Future.failed(new Exception(s"Failed to find main emotion id for sub emotion id: $subEmotionId"))
          }
        })
      case None =>
        Future.successful(None)
    }
  }

  private def fixEmotionTypeByMainEmotion(mainEmotionIdOpt: Option[String]): Future[Option[String]] = {
    mainEmotionIdOpt match {
      case Some(mainEmotionId) =>
        databaseExecutionContext.withConnection({ implicit connection =>
          val emotionTypeOpt = emotionRecordDao.findEmotionTypeByMainEmotionId(mainEmotionId)
          emotionTypeOpt match {
            case Some(emotionType) =>
              logger.info(s"Found emotion type for main emotion id: $emotionType, main emotion id: $mainEmotionId")
              Future.successful(Some(emotionType))
            case None =>
              logger.error(s"Failed to find emotion type for mainEmotionId: $mainEmotionId")
              Future.failed(new Exception(s"Failed to find emotion type  for mainEmotionId: $mainEmotionId"))
          }
        })
      case None =>
        Future.successful(None)
    }
  }

  override def delete(id: Long, userId: Long): Future[Boolean] = {
    Future(databaseExecutionContext.withConnection({ implicit connection =>
      val isEmotionRecordDeleted = emotionRecordDao.deleteByIdAndUserId(id, userId)
      if (isEmotionRecordDeleted) {
        noteService.deleteByEmotionRecordId(id, userId)
        tagService.deleteByEmotionRecordId(id, userId)
      }
      isEmotionRecordDeleted
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

  def updateWithEmotionDetectionResult(userId: Long, emotionRecordId: Long, emotionDetectionResult: EmotionDetectionResult): Future[Boolean] = {
    val triggerNameOpt = emotionDetectionResult.triggers match {
      case Some(triggers) if triggers.nonEmpty =>
        triggers.head.triggerName
      case _ => None
    }

    val triggerIdOptFut: Option[Future[Option[Long]]] = triggerNameOpt.map(triggerService.findByName(_).map(_.triggerId))

    convertOptionOfFutureOfOptionOfLongToFutureOfOptionOfLong(triggerIdOptFut).flatMap(triggerIdOpt => {
      val emotionRecord = EmotionRecord(
        id = Some(emotionRecordId),
        emotionType = emotionDetectionResult.emotionType.getOrElse("Unknown"),
        intensity = emotionDetectionResult.intensity,
        emotionId = emotionDetectionResult.mainEmotionId,
        userId = Some(userId),
        created = None,
        lastUpdated = None,
        notes = List(),
        tags = emotionDetectionResult.tags.getOrElse(List()),
        triggers = List(),
        subEmotions = List(),
        emotion = None,
        subEmotionId = emotionDetectionResult.subEmotionId,
        triggerId = triggerIdOpt
      )
      val updatedRecord = update(emotionRecord)
      updatedRecord.recover({
        case e: Exception =>
          logger.error("Failed to update emotion record with results of detection", e)
          throw e
      })
      updatedRecord.map(_ => true)
    })
  }

  private def convertOptionOfFutureOfOptionOfLongToFutureOfOptionOfLong(triggerIdOptFut: Option[Future[Option[Long]]]):
  Future[Option[Long]] = {
    triggerIdOptFut match {
      case Some(fut) => fut
      case None => Future.successful(None)
    }
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

  override def emotionRecordsToDoughnutEmotionTypeTriggerChartData(records: List[EmotionRecord]): DoughnutEmotionTypesTriggersChartData =
    DoughnutEmotionTypesTriggersChartData(
      emotionRecordsToDoughnutEmotionTypeChartData(records),
      emotionRecordsToDoughnutTriggerChartData(records)
    )

  private implicit def userIdOptToLong: Option[Long] => Long = {
    case Some(userId) => userId
    case None => throw new Exception("User id not found")
  }
}

