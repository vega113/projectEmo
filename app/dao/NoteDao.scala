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
    SQL("SELECT * FROM notes WHERE id = {id}").on("id" -> id).as(Note.parser.singleOpt)
  }

  def insert(note: Note)(implicit connection: Connection): Option[Long] = {
    SQL("""
      INSERT INTO notes (title, content, user_id)
      VALUES ({title}, {content}, {userId})
    """).on("title" -> note.title,
      "content" -> note.content,
      "userId" -> note.userId)
      .executeInsert()
  }

  def update(note: Note)(implicit connection: Connection): Int = {
    SQL("""
      UPDATE notes
      SET title = {title}, content = {content}, user_id = {userId}, last_updated = {lastUpdated}
      WHERE id = {id}
    """).on("id" -> note.id,
      "title" -> note.title,
      "content" -> note.content,
      "userId" -> note.userId,
      "lastUpdated" -> dateTimeService.now())
      .executeUpdate()
  }

  def delete(id: Int)(implicit connection: Connection): Int = {
    SQL("DELETE FROM notes WHERE id = {id}").on("id" -> id).executeUpdate()
  }
}
