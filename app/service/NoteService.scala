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
  def insert(userId: Long, emotionRecordId: Long, note: Note): Future[Option[Long]]

  def findAllNoteTemplates(): Future[List[NoteTemplate]]

  def delete(userId: Long, id: Long): Future[Boolean]

  def undelete(userId: Long, id: Long): Future[Boolean]
}

class NoteServiceImpl @Inject() (noteDao: NoteDao, tagDao: TagDao,
                                 emotionRecordService: EmotionRecordService,
                                 databaseExecutionContext: DatabaseExecutionContext) extends NoteService {

  private def makeTitle(text: String): String = {
    val maxLength = 30
    val firstLine = text.split("\n")(0)
    if (firstLine.length > maxLength) {
      firstLine.substring(0, maxLength)
    } else {
      firstLine
    }
  }

  private def extractTags(text: String): List[Tag] = {
    val tagRegex = "#[a-zA-Z0-9]+".r
    tagRegex.findAllIn(text).toList.map(tag => Tag(None, tag)).groupBy(_.tagName).map(_._2.head).toList
  }

  override def insert(userId: Long, emotionRecordId: Long, note: Note): Future[Option[Long]] = {
    val userEmotionRecordFutOpt = emotionRecordService.findByIdForUser(emotionRecordId, userId)
    userEmotionRecordFutOpt.flatMap {
      case Some(_) => databaseExecutionContext.withConnection({ implicit connection =>
        val title = note.title.getOrElse(makeTitle(note.text))
        val noteId: Long = noteDao.insert(emotionRecordId, note.copy(title = Some(title))) match {
          case Some(id) =>
            addNewTagsFromNoteToRecord(emotionRecordId, note)
            id
          case None => throw new Exception("Failed to insert note")
        }
        noteDao.linkNoteToEmotionRecord(noteId, emotionRecordId)
        Future.successful(Some(noteId))
      })
      case None => Future.successful(None)
    }
  }

  private def addNewTagsFromNoteToRecord(emotionRecordId: Long, note: Note)(implicit connection: Connection): List[Long] = {
    val tags = extractTags(note.text)
    val existingTags = tagDao.findAllByEmotionRecordId(emotionRecordId)
    val newTags = tags.filter(tag => !existingTags.map(_.tagName).contains(tag.tagName))
    tagDao.insert(emotionRecordId, newTags)
  }

  override def findAllNoteTemplates(): Future[List[NoteTemplate]] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      Future.successful(noteDao.findAllNoteTemplates())
    })
  }

  override def delete(userId: Long, id: Long): Future[Boolean] = {
    val emotionRecordIdOpt = emotionRecordService.findEmotionRecordIdByNoteId(id)
    emotionRecordIdOpt.flatMap {
      case Some(emotionRecordId) =>
        val userEmotionRecordFutOpt = emotionRecordService.findByIdForUser(emotionRecordId, userId)
        userEmotionRecordFutOpt.flatMap {
          case Some(_) =>
            databaseExecutionContext.withConnection({ implicit connection =>
              Future.successful(noteDao.delete(id) > 0)
            })
          case None => Future.successful(false)
        }
      case None => Future.successful(false)
    }
  }

  override def undelete(userId: Long, id: Long): Future[Boolean] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      val emotionRecordIdOpt = noteDao.findEmotionRecordIdByNoteId(id)
      emotionRecordIdOpt match {
        case Some(emotionRecordId) =>
          val userEmotionRecordFutOpt = emotionRecordService.findByIdForUser(emotionRecordId, userId)
          userEmotionRecordFutOpt.flatMap {
            case Some(_) => Future.successful(noteDao.undelete(id) > 0)
            case None => Future.successful(false)
          }
        case None => Future.successful(false)
      }
    })
  }
}