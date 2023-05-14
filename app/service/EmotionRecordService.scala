package service

import com.google.inject.{ImplementedBy, Inject}
import controllers.model
import dao.model.{EmotionRecord, EmotionRecordDay, EmotionRecordMonth, EmotionRecordWeek, SuggestedAction}
import dao.{DatabaseExecutionContext, EmotionRecordDao}

import java.time.LocalDate
import java.time.temporal.ChronoField
import scala.collection.immutable.ListMap
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
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

  def fetchRecordsForMonthByDate(userId: Long, date: LocalDate): Future[EmotionRecordMonth]
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

  private def groupRecordsByWeek(records: List[EmotionRecordDay]): List[EmotionRecordWeek] = {
    val recordsByWeek: Map[Int, List[EmotionRecordDay]] = records.groupBy(record => {
      record.date.get(ChronoField.ALIGNED_WEEK_OF_YEAR)
    })
    val out: List[EmotionRecordWeek] = recordsByWeek.map(entry => {
      EmotionRecordWeek(entry._1, entry._2)
    }).toList.sortWith((w1, w2) => {
      w1.week > w2.week
    })
    out
  }

  private def fillInEmotionRecordDaysIfMissing(date: LocalDate, days: List[EmotionRecordDay]): List[EmotionRecordDay] = {
    val minDayOfMonth = 1
    val maxDayOfMonth = date.lengthOfMonth()

    (minDayOfMonth to maxDayOfMonth).toList.flatMap(dayOfMonth => {
      days match {
        case Nil => Some(EmotionRecordDay(LocalDate.of(date.getYear, date.getMonth, dayOfMonth), List()))
        case _ => days.find(_.date.getDayOfMonth == dayOfMonth)
          .orElse(Some(EmotionRecordDay(LocalDate.of(days.head.date.getYear, days.head.date.getMonth, dayOfMonth), List())))
      }
    })
  }

  def fetchRecordsForMonthByDate(userId: Long, date: LocalDate): Future[EmotionRecordMonth] = {
    Future.successful(databaseExecutionContext.withConnection({ implicit connection =>
      val startDate = date.withDayOfMonth(1)
      val endDate = date.withDayOfMonth(date.lengthOfMonth())

      val records: List[EmotionRecord] = emotionRecordDao.findAllByUserIdAndDateRange(
        userId, startDate.toString, endDate.toString)
      val recordsByDay: List[EmotionRecordDay] = fillInEmotionRecordDaysIfMissing(date, groupRecordsByDate(records))
      val recordsByWeek: List[EmotionRecordWeek] = groupRecordsByWeek(recordsByDay)
      val recordsByMonth = EmotionRecordMonth(date.withDayOfMonth(1), recordsByWeek)
      recordsByMonth
    }))
  }
}

