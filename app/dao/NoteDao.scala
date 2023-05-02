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

  def findAll()(implicit connection: Connection): List[Note] = {
    SQL("SELECT * FROM notes").as(Note.parser.*)
  }

  def findById(id: Int)(implicit connection: Connection): Option[Note] = {
    SQL("SELECT * FROM notes WHERE noteId = {id}").on("id" -> id).as(Note.parser.singleOpt)
  }

  def insert(note: Note)(implicit connection: Connection): Option[Long] = {
    SQL(
      """
      INSERT INTO notes (title, text, id, created)
      VALUES ({title}, {text}, {created})""").
      on("title" -> note.title, "text" -> note.text,
        "created" -> dateTimeService.now())
      .executeInsert()
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

  def delete(id: Int)(implicit connection: Connection): Int = {
    SQL("DELETE FROM notes WHERE note_id = {note_id}").on("note_id" -> id).executeUpdate()
  }
}
