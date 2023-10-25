package service

import com.google.inject.ImplementedBy
import dao.{DatabaseExecutionContext, NoteDao, TagDao}

import javax.inject.Inject
import scala.concurrent.Future

@ImplementedBy(classOf[TagServiceImpl])
trait TagService {
  def delete(emotionRecordId: Long, noteId: Long): Future[Boolean]
}

class TagServiceImpl @Inject() (tagDao: TagDao, databaseExecutionContext: DatabaseExecutionContext) extends TagService {
  override def delete(emotionRecordId: Long, tagId: Long): Future[Boolean] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      val count = tagDao.delete(emotionRecordId, tagId)
      Future.successful(count > 0)
    })
  }
}
