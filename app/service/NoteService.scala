package service

import com.google.inject.ImplementedBy
import dao.{DatabaseExecutionContext, NoteDao, TagDao}
import dao.model.{Note, NoteTemplate, Tag}

import java.sql.Connection
import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@ImplementedBy(classOf[NoteServiceImpl])
trait NoteService {
  def insert(emotionRecordId: Long, note: Note): Future[Option[Long]]

  def findAllNoteTemplates(): Future[List[NoteTemplate]]

  def delete(userId: Long, id: Long): Future[Boolean]

  def undelete(userId: Long, id: Long): Future[Boolean]

  def findEmotionRecordIdByNoteId(noteId: Long): Future[Option[Long]]

  def makeTitle(text: String): String

  def extractTags(text: String): Set[Tag]
}

class NoteServiceImpl @Inject() (noteDao: NoteDao, tagDao: TagDao,
                                 databaseExecutionContext: DatabaseExecutionContext) extends NoteService {

  def makeTitle(text: String): String = {
    val maxLength = 30
    val firstLine = text.split("\n")(0)
    if (firstLine.length > maxLength) {
      firstLine.substring(0, maxLength)
    } else {
      firstLine
    }
  }

  def extractTags(text: String): Set[Tag] = {
    val tagRegex = "(?<=#)[a-zA-Z0-9]+".r
    tagRegex.findAllIn(text).toList.map(tag => Tag(None, tag)).groupBy(_.tagName).map(_._2.head).toSet
  }


  override def insert(emotionRecordId: Long, note: Note): Future[Option[Long]] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      val title = note.title.getOrElse(makeTitle(note.text))
      val noteId: Long = noteDao.insert(emotionRecordId, note.copy(title = Some(title))) match {
        case Some(id) =>
          addNewTagsFromNoteToRecord(emotionRecordId, note)
          id
        case None => throw new Exception("Failed to insert note")
      }
      Future.successful(Some(noteId))
    })
  }

  private def addNewTagsFromNoteToRecord(emotionRecordId: Long, note: Note)(implicit connection: Connection): List[Long] = {
    val tags = extractTags(note.text)
    val existingTags = tagDao.findAllByEmotionRecordId(emotionRecordId)
    val newTags = tags.filter(tag => !existingTags.map(_.tagName).contains(tag.tagName))
    tagDao.insert(emotionRecordId, newTags.toList)
  }

  override def findAllNoteTemplates(): Future[List[NoteTemplate]] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      Future.successful(noteDao.findAllNoteTemplates())
    })
  }

  override def delete(emotionRecordId: Long, noteId: Long): Future[Boolean] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      Future.successful(noteDao.delete(emotionRecordId, noteId) > 0)
    })
  }

  override def undelete(emotionRecordId: Long, id: Long): Future[Boolean] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      Future.successful(noteDao.undelete(emotionRecordId, id) > 0)
    })
  }

  override def findEmotionRecordIdByNoteId(noteId: Long): Future[Option[Long]] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      Future.successful(noteDao.findEmotionRecordIdByNoteId(noteId))
    })
  }
}