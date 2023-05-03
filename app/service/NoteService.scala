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
    databaseExecutionContext.withConnection({ implicit connection =>
      emotionRecordService.findByIdForUser(emotionRecordId, userId).map {
        case Some(_) =>
          val noteIdOpt = noteDao.insert(emotionRecordId, note)
          noteDao.linkNoteToEmotionRecord(noteIdOpt.get, emotionRecordId)
          noteIdOpt
        case None => None
      }
    })
  }
}