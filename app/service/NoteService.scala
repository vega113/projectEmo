package service

import com.google.inject.ImplementedBy
import dao.{DatabaseExecutionContext, NoteDao, TagDao}
import dao.model.{Note, NoteTemplate, Tag}
import net.logstash.logback.argument.StructuredArguments._

import java.sql.Connection
import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.sequence

@ImplementedBy(classOf[NoteServiceImpl])
trait NoteService {
  def insert(emotionRecordId: Long, note: Note): Future[Option[Long]]

  def insert(emotionRecordId: Long, notes: List[Note]): Future[Option[Long]]

  def findAllNoteTemplates(): Future[List[NoteTemplate]]

  def delete(userId: Long, id: Long): Future[Boolean]

  def undelete(userId: Long, id: Long): Future[Boolean]

  def findEmotionRecordIdByNoteId(noteId: Long): Future[Option[Long]]

  def extractTags(text: String): Set[Tag]
}

class NoteServiceImpl @Inject() (noteDao: NoteDao, tagDao: TagDao,
                                 databaseExecutionContext: DatabaseExecutionContext,
                                 titleService: TitleService,
                                  todoService: NoteTodoService
                                ) extends NoteService {

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  def extractTags(text: String): Set[Tag] = {
    val tagRegex = "(?<=#)[a-zA-Z0-9]+".r
    tagRegex.findAllIn(text).toList.map(tag => Tag(None, tag)).groupBy(_.tagName).map(_._2.head).toSet
  }


  override def insert(emotionRecordId: Long, note: Note): Future[Option[Long]] = {
    insert(emotionRecordId, List(note))
  }

  override def insert(emotionRecordId: Long, notes: List[Note]): Future[Option[Long]] = {
    logger.info("Inserting notes for emotion record, count: {} {}", emotionRecordId, notes.length)
    databaseExecutionContext.withConnection({ implicit connection =>
      val noteIds = notes.map(note => {
        insertOne(emotionRecordId, note)
      })
      Future.successful(noteIds.head)
    })
  }

  private def insertOne(emotionRecordId: Long, note: Note)(implicit connection: Connection): Option[Long] = {
    noteDao.insert(emotionRecordId, note.copy(title = makeTitle(note))) match {
      case x@Some(id) =>
        addNewTagsFromNoteToRecord(emotionRecordId, note)
        addTodosFromNoteToNote(note.text, id)
        logger.info("Inserted note emotion record id {}, note id {}", value("noteId", emotionRecordId),
          value("noteId", id))
        x
      case None => throw new Exception("Failed to insert note")
    }
  }

  private def makeTitle(note: Note): Option[String] = {
    Option(titleService.makeTitle(note.text))
  }

  private def addNewTagsFromNoteToRecord(emotionRecordId: Long, note: Note)(implicit connection: Connection): Set[Long] = {
    val tags = extractTags(note.text)
    logger.info("Extracted tags from note: {}, emotionRecordId: {}", value("tags", tags),
      value("emotionRecordId", emotionRecordId))
    val existingTags = tagDao.findAllByEmotionRecordId(emotionRecordId)
    val newTags = tags.filter(tag => !existingTags.map(_.tagName).contains(tag.tagName))
    val tagId = tagDao.insert(emotionRecordId, newTags)
    logger.info("Inserted tags from note: {}, emotionRecordId: {}", value("tags", newTags),
      value("emotionRecordId", emotionRecordId))
    tagId
  }

  private def addTodosFromNoteToNote(text: String, noteId: Long): Future[List[Option[Long]]] = {
    val todos = todoService.extractTodos(text)
    sequence(todos.map(todo => todoService.insert(text, noteId, todo)))
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