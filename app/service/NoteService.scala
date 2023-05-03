package service

import com.google.inject.ImplementedBy
import dao.{DatabaseExecutionContext, NoteDao}
import dao.model.Note

import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@ImplementedBy(classOf[NoteServiceImpl])
trait NoteService {
  def insert(userId: Long, emotionRecordId: Long, note: Note): Future[Option[Long]]

}

class NoteServiceImpl @Inject() (noteDao: NoteDao,
                      emotionRecordService: EmotionRecordService,
                      databaseExecutionContext: DatabaseExecutionContext) extends NoteService {
  override def insert(userId: Long, emotionRecordId: Long, note: Note): Future[Option[Long]] = {
    val userFutOpt = emotionRecordService.findByIdForUser(emotionRecordId, userId)
    userFutOpt.flatMap {
      case Some(_) => databaseExecutionContext.withConnection({ implicit connection =>
        val noteId: Long = noteDao.insert(emotionRecordId, note) match {
          case Some(id) => id
          case None => throw new Exception("Failed to insert note")
        }
        noteDao.linkNoteToEmotionRecord(noteId, emotionRecordId)
        Future.successful(Some(noteId))
      })
      case None => Future.successful(None)
    }
  }
}