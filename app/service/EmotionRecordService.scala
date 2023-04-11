package service

import com.google.inject.{ImplementedBy, Inject}
import dao.model.EmotionRecord
import dao.{DatabaseExecutionContext, EmotionRecordDao}

import scala.concurrent.Future

@ImplementedBy(classOf[EmotionRecordServiceImpl])
trait EmotionRecordService {
  def findAll(): Future[List[EmotionRecord]]
  def findById(id: Long): Future[Option[EmotionRecord]]
  def findAllByUserId(userId: Long): Future[List[EmotionRecord]]
  def insert(emotionRecord: EmotionRecord): Future[Option[Long]]
  def update(emotionRecord: EmotionRecord): Future[Int]
  def delete(id: Long): Future[Int]
}

class EmotionRecordServiceImpl @Inject()(
                                          emotionRecordDao: EmotionRecordDao,
                                          databaseExecutionContext: DatabaseExecutionContext
                                        ) extends EmotionRecordService {
  override def findAll(): Future[List[EmotionRecord]] = {
    Future.successful(databaseExecutionContext.withConnection { implicit connection =>
      emotionRecordDao.findAll()
    })
  }

  override def findById(id: Long): Future[Option[EmotionRecord]] = {
    Future.successful(databaseExecutionContext.withConnection({ implicit connection =>
      emotionRecordDao.findById(id)
    }))
  }

  override def findAllByUserId(userId: Long): Future[List[EmotionRecord]] = {
    Future.successful(databaseExecutionContext.withConnection({ implicit connection =>
      emotionRecordDao.findAllByUserId(userId)
    }))
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

