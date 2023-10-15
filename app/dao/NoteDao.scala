package dao

import anorm._
import dao.model._
import service.DateTimeService

import java.sql.Connection
import javax.inject.Inject

class NoteDao @Inject()(dateTimeService: DateTimeService) {
  def findAllByEmotionRecordId(id: Long)(implicit connection: Connection): List[Note] = {
    SQL("SELECT * FROM notes inner join emotion_record_notes on id = note_id  WHERE emotion_record_id = {id}").on("id" -> id).as(Note.parser.*)
  }

  def findAllNotDeletedByEmotionRecordId(id: Long)(implicit connection: Connection): List[Note] = {
    SQL("SELECT * FROM notes inner join emotion_record_notes on id = note_id  WHERE emotion_record_id = {id} and is_deleted = false").on("id" -> id).as(Note.parser.*)
  }


  def findAll()(implicit connection: Connection): List[Note] = {
    SQL("SELECT * FROM notes").as(Note.parser.*)
  }

  def findById(id: Long)(implicit connection: Connection): Option[Note] = {
    SQL("SELECT * FROM notes WHERE noteId = {id}").on("id" -> id).as(Note.parser.singleOpt)
  }

  def insert(emotionRecordId: Long, note: Note)(implicit connection: Connection): Option[Long] = {
    val noteIdOpt: Option[Long] = SQL(
      """
      INSERT INTO notes (title, text)
      VALUES ({title}, {text})""").
      on("title" -> note.title, "text" -> note.text).
      executeInsert()
    noteIdOpt
  }

  def linkNoteToEmotionRecord(noteId: Long, emotionRecordId: Long)(implicit connection: Connection): Int = {
    SQL(
      """
      INSERT INTO emotion_record_notes (note_id, emotion_record_id)
      VALUES ({noteId}, {emotionRecordId})""").
      on("noteId" -> noteId, "emotionRecordId" -> emotionRecordId).executeUpdate()
  }

  def findEmotionRecordIdByNoteId(noteId: Long)(implicit connection: Connection): Option[Long] = {
    SQL("SELECT emotion_record_id FROM emotion_record_notes WHERE note_id = {noteId}").on("noteId" -> noteId).as(SqlParser.scalar[Long].singleOpt)
  }

  def update(note: Note)(implicit connection: Connection): Int = {
    SQL(
      """
      UPDATE notes
      SET title = {title}, content = {content}, user_id = {userId}, last_updated = {lastUpdated}
      WHERE id = {id}
    """).on("note_id" -> note.id,
      "title" -> note.title,
      "text" -> note.text,
      "created" -> dateTimeService.now())
      .executeUpdate()
  }

  def delete(id: Long)(implicit connection: Connection): Int = {
    SQL(
      """
      UPDATE notes
      SET is_deleted = true, last_deleted = {lastDeleted}
      WHERE id = {id}
    """).on("id" -> id,
      "lastDeleted" -> dateTimeService.now())
      .executeUpdate()
  }

  def undelete(id: Long)(implicit connection: Connection): Int = {
    SQL(
      """
      UPDATE notes
      SET is_deleted = false, last_updated = {lastUpdated}
      WHERE id = {noteId}
    """).on("id" -> id,
      "lastUpdated" -> dateTimeService.now())
      .executeUpdate()
  }

  def findAllNoteTemplates()(implicit connection: Connection): List[NoteTemplate] = {
    SQL("SELECT * FROM note_template").as(NoteTemplate.parser.*)
  }
}
