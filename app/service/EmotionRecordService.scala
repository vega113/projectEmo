package service

import com.google.inject.{ImplementedBy, Inject}
import controllers.model
import dao.model.{EmotionRecord, EmotionRecordDay, SuggestedAction, SunburstData}
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
  def groupRecordsByDate(records: List[EmotionRecord]): List[EmotionRecordDay]
  def fetchRecordsForMonthByDate(userId: Long, startDateTime: Instant, endDateTime: Instant): Future[List[EmotionRecord]]
  def emotionRecordsToChartData(records: List[EmotionRecord]): List[SunburstData]
}

class EmotionRecordServiceImpl @Inject()(
                                          emotionRecordDao: EmotionRecordDao,
                                          emotionDataService: EmotionDataService,
                                          databaseExecutionContext: DatabaseExecutionContext
                                        ) extends EmotionRecordService {
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

  override def insert(emotionRecord: EmotionRecord): Future[Option[Long]] = {
    Future.successful(databaseExecutionContext.withConnection({ implicit connection =>
      emotionRecordDao.insert(emotionRecord)
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

  import model._

  import model._

  import model._

  import model._

  import model._

  private def computeColor(name: String): Option[String] = {
    name match {
      case "Positive" => Some("green")
      case "Negative" => Some("red")
      case "Neutral" => Some("blue")
      case _ => Some("gray")
    }
  }

  def emotionRecordsToChartData(records: List[EmotionRecord]): List[SunburstData] = {
    // First grouping by emotionType
    val recordsByType: Map[String, List[EmotionRecord]] = records.groupBy(_.emotionType)

    val chartData = recordsByType.flatMap { case (emotionType, recordsForType) =>
      // Second grouping by emotionName within each emotionType
      val recordsByEmotion: Map[String, List[EmotionRecord]] = recordsForType.groupBy(record => record.emotion.flatMap(_.emotionName).getOrElse("undefined"))

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



}

