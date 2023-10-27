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
    val noteId: Option[Long] = SQL(
      """
          INSERT INTO notes (title, text, description, suggestion)
          VALUES ({title}, {text}, {description}, {suggestion})""").
      on("title" -> note.title, "text" -> note.text, "description" -> note.description, "suggestion" -> note.suggestion).
      executeInsert()
    noteId.foreach(id => linkNoteToEmotionRecord(id, emotionRecordId))
    noteId
  }

  private def linkNoteToEmotionRecord(noteId: Long, emotionRecordId: Long)(implicit connection: Connection): Int = {
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

  def delete(emotionRecordId: Long, noteId: Long)(implicit connection: Connection): Int = {
    if(checkNoteExistsForEmotionRecord(emotionRecordId, noteId)){
      SQL(
        """
        UPDATE notes
        SET is_deleted = true, last_deleted = {lastDeleted}
        WHERE id = {id}
      """).on("id" -> noteId,
          "lastDeleted" -> dateTimeService.now())
        .executeUpdate()
    } else {
      0
    }

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
