package dao

import anorm._
import dao.model._
import services.DateTimeService

import java.sql.Connection
import javax.inject.Inject

class NoteDao @Inject() (dateTimeService: DateTimeService) {
  def findAll()(implicit connection: Connection): List[Note] = {
    SQL("SELECT * FROM notes").as(Note.parser.*)
  }

  def findById(id: Int)(implicit connection: Connection): Option[Note] = {
    SQL("SELECT * FROM notes WHERE noteId = {id}").on("id" -> id).as(Note.parser.singleOpt)
  }

  def insert(note: Note)(implicit connection: Connection): Option[Long] = {
    SQL("""
      INSERT INTO notes (title, note_text, note_user_id, created)
      VALUES ({title}, {noteText}, {noteUserId}, {created})
    """).on("title" -> note.title,
      "note_text" -> note.noteText,
      "note_user_id" -> note.noteUserId,
      "created" -> dateTimeService.now())
      .executeInsert()
  }

  def update(note: Note)(implicit connection: Connection): Int = {
    SQL("""
      UPDATE notes
      SET title = {title}, content = {content}, user_id = {userId}, last_updated = {lastUpdated}
      WHERE id = {id}
    """).on("note_id" -> note.noteId,
      "title" -> note.title,
      "note_text" -> note.noteText,
      "note_user_id" -> note.noteUserId,
      "created" -> dateTimeService.now())
      .executeUpdate()
  }

  def delete(id: Int)(implicit connection: Connection): Int = {
    SQL("DELETE FROM notes WHERE note_id = {note_id}").on("note_id" -> id).executeUpdate()
  }
}
