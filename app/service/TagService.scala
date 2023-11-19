package service

import com.google.inject.ImplementedBy
import dao.model.Tag
import dao.{DatabaseExecutionContext, TagDao}

import javax.inject.Inject
import scala.concurrent.Future

@ImplementedBy(classOf[TagServiceImpl])
trait TagService {

  def insert(emotionRecordId: Long, tags: Set[Tag]): Future[Boolean]
  def delete(emotionRecordId: Long, noteId: Long): Future[Boolean]
  def insert(emotionRecordId: Long, tagName: String): Future[Boolean]
}

class TagServiceImpl @Inject() (tagDao: TagDao, databaseExecutionContext: DatabaseExecutionContext) extends TagService {
  override def delete(emotionRecordId: Long, tagId: Long): Future[Boolean] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      val count = tagDao.delete(emotionRecordId, tagId)
      Future.successful(count > 0)
    })
  }

  override def insert(emotionRecordId: Long, tagName: String): Future[Boolean] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      val count = tagDao.insert(emotionRecordId, tagName)
      Future.successful(count > 0)
    })
  }

  override def insert(emotionRecordId: Long, tags: Set[Tag]): Future[Boolean] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      val tagIds = tagDao.insert(emotionRecordId, tags)
      Future.successful(tagIds.toList.length == tags.size)
    })
  }
}
