package service

import com.google.inject.{ImplementedBy, Inject}
import controllers.model
import dao.model.{EmotionRecord, SuggestedAction}
import dao.{DatabaseExecutionContext, EmotionRecordDao}

import java.time.LocalDate
import scala.collection.immutable.ListMap
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.math.Ordered.orderingToOrdered

@ImplementedBy(classOf[EmotionRecordServiceImpl])
trait EmotionRecordService {
  def findAll(): Future[List[EmotionRecord]]
  def findByIdForUser(recordId: Long, userId: Long): Future[Option[EmotionRecord]]
  def findAllByUserId(userId: Long): Future[List[EmotionRecord]]
  def insert(emotionRecord: EmotionRecord): Future[Option[Long]]
  def update(emotionRecord: EmotionRecord): Future[Int]
  def delete(id: Long): Future[Int]
  def findSuggestionsByEmotionRecord(record: EmotionRecord): Future[List[SuggestedAction]]
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
}

