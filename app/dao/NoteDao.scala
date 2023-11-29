package dao

import anorm._
import dao.model._
import service.DateTimeService

import java.sql.Connection
import javax.inject.Inject

class NoteDao @Inject()(dateTimeService: DateTimeService, noteTodoDao: NoteTodoDao) {

  def deleteByEmotionRecordId(id: Long, emotionRecordId: Long)(implicit connection: Connection): Int = {
    SQL(
      """
        |DELETE FROM notes
        |WHERE id = {id} and emotion_record_id = {emotionRecordId}
        |""".stripMargin).on("id" -> id, "emotionRecordId" -> emotionRecordId).executeUpdate()
  }

  def findAllByEmotionRecordId(id: Long)(implicit connection: Connection): List[Note] = {
    SQL("SELECT * FROM notes inner join emotion_record_notes on id = note_id  WHERE emotion_record_id = {id}").on("id" -> id).as(Note.parser.*)
  }

  def findAllNotDeletedByEmotionRecordId(id: Long)(implicit connection: Connection): List[Note] = {
    val notes: List[Note] =
      SQL("SELECT * FROM notes " +
        "  WHERE emotion_record_id = {id} and is_deleted <> true").on("id" -> id).as(Note.parser.*)
        for {
          note <- notes
          noteId <- note.id
        } yield {
          note.copy(
            todos = noteTodoDao.findNoteTodosByNoteId(noteId)
          )
        }
  }


  def findAll()(implicit connection: Connection): List[Note] = {
    SQL("SELECT * FROM notes").as(Note.parser.*)
  }

  def findById(id: Long)(implicit connection: Connection): Option[Note] = {
    SQL("SELECT * FROM notes WHERE noteId = {id}").on("id" -> id).as(Note.parser.singleOpt)
  }

  def insert(note: Note)(implicit connection: Connection): Option[Long] = {
    val noteId: Option[Long] = SQL(
      """
          INSERT INTO notes (title, text, description, suggestion, emotion_record_id)
          VALUES ({title}, {text}, {description}, {suggestion}, {emotionRecordId})""").
      on("title" -> note.title, "text" -> note.text, "description" -> note.description, "suggestion" -> note.suggestion,
      "emotionRecordId" -> note.emotionRecordId).
      executeInsert()
    noteId
  }

  def findEmotionRecordIdByNoteId(noteId: Long)(implicit connection: Connection): Option[Long] = {
    SQL("SELECT emotion_record_id FROM emotion_record_notes WHERE note_id = {noteId}").on("noteId" -> noteId).as(SqlParser.scalar[Long].singleOpt)
  }

  def update(note: Note)(implicit connection: Connection): Int = {
    SQL(
      """
      UPDATE notes
      SET title = {title}, content = {content}, last_updated = {lastUpdated},
       is_deleted = {isDeleted}, description = {description}, suggestion = {suggestion}
      WHERE id = {id} and user_id = {userId}
    """).on("note_id" -> note.id,
        "title" -> note.title,
        "text" -> note.text,
        "description" -> note.description,
        "suggestion" -> note.suggestion,
        "lastUpdated" -> dateTimeService.now(),
        "isDeleted" -> note.isDeleted,
        "created" -> dateTimeService.now())
      .executeUpdate()
  }


  def undelete(emotionRecordId: Long, noteId: Long)(implicit connection: Connection): Int = {
    if(checkNoteExistsForEmotionRecord(emotionRecordId, noteId)){
      SQL(
        """
        UPDATE notes
        SET is_deleted = false, last_updated = {lastUpdated}
        WHERE id = {noteId}
      """).on("id" -> noteId,
          "lastUpdated" -> dateTimeService.now())
        .executeUpdate()
    } else {
      0
    }
  }

  def findAllNoteTemplates()(implicit connection: Connection): List[NoteTemplate] = {
    SQL("SELECT * FROM note_template").as(NoteTemplate.parser.*)
  }

  private def checkNoteExistsForEmotionRecord(emotionRecordId: Long, noteId: Long)(implicit connection: Connection): Boolean = {
    SQL("SELECT count(*) FROM notes inner join emotion_record_notes on id = note_id  WHERE emotion_record_id = " +
      "{emotionRecordId} and note_id = {noteId}").
      on("emotionRecordId" -> emotionRecordId, "noteId" -> noteId).as(SqlParser.scalar[Int].single) > 0
  }
}
