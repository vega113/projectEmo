package service

import com.google.inject.ImplementedBy
import dao.{DatabaseExecutionContext, TagDao}

import javax.inject.Inject
import scala.concurrent.Future

@ImplementedBy(classOf[TagServiceImpl])
trait TagService {
  def delete(emotionRecordId: Long, noteId: Long): Future[Boolean]
  def add(emotionRecordId: Long, tagName: String): Future[Boolean]
}

class TagServiceImpl @Inject() (tagDao: TagDao, databaseExecutionContext: DatabaseExecutionContext) extends TagService {
  override def delete(emotionRecordId: Long, tagId: Long): Future[Boolean] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      val count = tagDao.delete(emotionRecordId, tagId)
      Future.successful(count > 0)
    })
  }

  override def add(emotionRecordId: Long, tagName: String): Future[Boolean] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      val count = tagDao.add(emotionRecordId, tagName)
      Future.successful(count > 0)
    })
  }
}
