package service

import com.google.inject.ImplementedBy
import dao.{DatabaseExecutionContext, NoteDao, TagDao, model}
import dao.model.{Note, NoteTemplate, Tag}
import net.logstash.logback.argument.StructuredArguments._

import java.sql.Connection
import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.sequence
import scala.language.implicitConversions
import scala.util.{Failure, Success}

@ImplementedBy(classOf[NoteServiceImpl])
trait NoteService {
  def deleteByEmotionRecordId(id: Long, userId: Long): Boolean

  def insert(note: Note): Future[Option[Long]]

  def insert(notes: List[Note]): Future[Option[Long]]

  def findAllNoteTemplates(): Future[List[NoteTemplate]]

  def delete(userId: Long, id: Long): Future[Boolean]

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

  override def insert(note: Note): Future[Option[Long]] = {
    insert(List(note))
  }

  override def insert(notes: List[Note]): Future[Option[Long]] = {
    validateNote(notes)
    logger.info("Inserting notes for emotion record, count: {} {}", notes.head.emotionRecordId, notes.length)
    databaseExecutionContext.withConnection({ implicit connection =>
      val noteIds = notes.map(note => {
        insertOne(note)
      })
      Future.successful(noteIds.head)
    })
  }

  private def validateNote(notes: List[Note]): Unit = {
    notes.foreach(note => {
      if (note.emotionRecordId.isEmpty) {
        throw new Exception("Emotion record id is not defined")
      }
      if (note.userId.isEmpty) {
        throw new Exception("User id is not defined")
      }
    })
  }

  private def insertOne(note: Note)(implicit connection: Connection): Option[Long] = {
    val titleWithEmotion = makeTitle(note)
    val updatedNote = note.copy(title = titleWithEmotion)
    val noteIdOpt = noteDao.insert(updatedNote)

    noteIdOpt match {
      case Some(noteId) =>
        try {
          val noteWithUpdatedIdAndAiTodos = updateNoteWithNoteId(updatedNote, noteId)
          addNewTagsFromNoteToRecord(noteWithUpdatedIdAndAiTodos)
          addTodosFromNoteToNote(noteWithUpdatedIdAndAiTodos).onComplete {
            case Success(_) => logger.info("Inserted note todos for note id {}", value("noteId", noteId))
            case Failure(ex) => logger.error("Failed to insert note todos for note id {}", value("noteId",
              noteId), ex)
          }
          addTodosFromAi(noteWithUpdatedIdAndAiTodos.todos).onComplete {
            case Success(_) => logger.info("Inserted ai todos for note id {}", value("noteId", noteId))
            case Failure(ex) => logger.error("Failed to insert ai todos for note id {}", value("noteId",
              noteId), ex)
          }
          logger.info("Inserted note with emotion record id {}, note id {}", value("emotionRecordId",
            note.emotionRecordId), value("noteId", noteId))
        } catch {
          case ex: Exception =>
            logger.error("An error occurred while processing note with id {}", value("noteId", noteId),
              ex)
        }
        Some(noteId)
      case None =>
        logger.error("Failed to insert note for emotion record id {}", value("emotionRecordId",
          note.emotionRecordId))
        None
    }
  }


  private def updateNoteWithNoteId(updatedNote: Note, noteId: Long) = {
    val updatedNotedWithId = updatedNote.copy(id = Some(noteId))
    updatedNotedWithId.todos match {
      case Some(aiTodos) => updatedNotedWithId.copy(todos = Some(aiTodos.map(
        todo => todo.copy(noteId = Some(noteId), userId = updatedNotedWithId.userId,
          emotionRecordId = updatedNotedWithId.emotionRecordId)
      )))
      case None => updatedNotedWithId
    }
  }

  private def makeTitle(note: Note): Option[String] = {
    note.title match {
      case None | Some("") => Option(titleService.makeTitle(note.text))
      case Some(title) if title.nonEmpty => Some(title)
      case _ => None
    }
  }

  private def addNewTagsFromNoteToRecord(note: Note)(implicit connection: Connection): Set[Long] = {
    val tags = extractTags(note.text)
    logger.info("Extracted tags from note: {}, emotionRecordId: {}", value("tags", tags),
      value("emotionRecordId", note.emotionRecordId))
    val existingTags = tagDao.findAllByEmotionRecordId(note.emotionRecordId)
    val newTags = tags.filter(tag => !existingTags.map(_.tagName).contains(tag.tagName))
    val tagId = tagDao.insert(note.emotionRecordId, note.userId, newTags)
    logger.info("Inserted tags from note: {}, emotionRecordId: {}", value("tags", newTags),
      value("emotionRecordId", note.emotionRecordId))
    tagId
  }



  private def addTodosFromNoteToNote(note: Note): Future[List[Option[Long]]] = {
    val extractedTodos = todoService.extractTodos(note)
    sequence(extractedTodos.map(todo => todoService.insert(todo)))
  }

  private def addTodosFromAi(todosOpt: Option[List[model.NoteTodo]]): Future[List[Option[Long]]] = {
    todosOpt match {
      case Some(todos) =>
        sequence(todos.map(todo => todoService.insert(todo.copy(isAi = Some(true), isAccepted = Some(false)))))
      case None => Future.successful(List())
    }
  }

  override def findAllNoteTemplates(): Future[List[NoteTemplate]] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      Future.successful(noteDao.findAllNoteTemplates())
    })
  }

  override def delete(emotionRecordId: Long, noteId: Long): Future[Boolean] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      Future.successful(noteDao.deleteByEmotionRecordId(noteId, emotionRecordId) > 0)
    })
  }


  override def findEmotionRecordIdByNoteId(noteId: Long): Future[Option[Long]] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      Future.successful(noteDao.findEmotionRecordIdByNoteId(noteId))
    })
  }

  override def deleteByEmotionRecordId(id: Long, userId: Long): Boolean = {
    databaseExecutionContext.withConnection({ implicit connection =>
      val count = noteDao.deleteByEmotionRecordId(id, userId) > 0
      logger.info("Deleted notes for emotion record {}", value("emotionRecordId", id))
      count
    })
  }

  implicit def idOptToLong(idOpt: Option[Long]): Long = {
    idOpt match {
      case Some(id) => id
      case None => throw new Exception("Id is None")
    }
  }
}