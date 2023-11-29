package service

import com.google.inject.ImplementedBy
import dao.model.Tag
import dao.{DatabaseExecutionContext, TagDao}

import javax.inject.Inject
import scala.concurrent.Future

@ImplementedBy(classOf[TagServiceImpl])
trait TagService {
  def deleteByEmotionRecordId(id: Long, userId: Long) : Future[Boolean]


  def insert(emotionRecordId: Long, userId: Long, tags: Set[Tag]): Future[Boolean]
  def delete(userId: Long, tagId: Long): Future[Boolean]
  def insert(emotionRecordId: Long, userId: Long, tagName: String): Future[Boolean]
}

class TagServiceImpl @Inject() (tagDao: TagDao, databaseExecutionContext: DatabaseExecutionContext) extends TagService {
  override def delete(userId: Long, tagId: Long): Future[Boolean] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      val count = tagDao.delete(userId, tagId)
      Future.successful(count > 0)
    })
  }

  override def insert(emotionRecordId: Long, userId: Long, tagName: String): Future[Boolean] = {
    insert(emotionRecordId, userId, Set(Tag(None, tagName)))
  }

  override def insert(emotionRecordId: Long, userId: Long, tags: Set[Tag]): Future[Boolean] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      val tagIds = tagDao.insert(emotionRecordId, userId, tags)
      Future.successful(tagIds.count(_ > 0) == tags.size)
    })
  }

  override def deleteByEmotionRecordId(id: Long, userId: Long): Future[Boolean] =
    databaseExecutionContext.withConnection({ implicit connection =>
      val count = tagDao.deleteByEmotionRecordId(id, userId)
      Future.successful(count > 0)
    })
}
