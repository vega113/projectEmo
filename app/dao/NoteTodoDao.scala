package dao

import anorm.{SQL, SqlParser}
import dao.model.{Note, NoteTodo}
import service.DateTimeService

import java.sql.Connection
import javax.inject.Inject

class NoteTodoDao @Inject()() {
  def findById(id: Long)(implicit connection: Connection): Option[NoteTodo] = {
    SQL("SELECT * FROM note_todos WHERE id = {id}").on("id" -> id).as(NoteTodo.parser.singleOpt)
  }

  def verifyNoteTodoBelongsToUser(userId: Long, noteTodoId: Long)(implicit connection: Connection): Boolean = {
    SQL(
      """
    SELECT emotion_records.user_id
    FROM note_todos
    INNER JOIN note_note_todos ON note_todos.id = note_note_todos.note_todo_id
    INNER JOIN notes ON note_note_todos.note_id = notes.id
    INNER JOIN emotion_record_notes ON notes.id = emotion_record_notes.note_id
    INNER JOIN emotion_records ON emotion_record_notes.emotion_record_id = emotion_records.id
    WHERE note_todos.id = {noteTodoId} AND emotion_records.user_id = {userId}
    """).on(
      "noteTodoId" -> noteTodoId,
      "userId" -> userId
    ).as(SqlParser.scalar[Long].singleOpt).isDefined
  }

  def acceptNoteTodo(noteTodoId: Long)(implicit connection: Connection): Boolean = {
    val updated: Int = SQL(
      """
    UPDATE note_todos
    SET is_accepted = true
    WHERE id = {noteTodoId}
    """).on(
      "noteTodoId" -> noteTodoId
    ).executeUpdate()
    updated == 1
  }


  def insert(noteId: Long, todo: NoteTodo)(implicit connection: Connection): Option[Long] = {
    val todoId: Option[Long] = SQL("""
    INSERT INTO note_todos (title, description, is_accepted, is_ai)
    VALUES ({title}, {description}, {is_accepted}, {is_ai})
  """).on(
      "title" -> todo.title,
      "description" -> todo.description,
      "is_accepted" -> todo.isAccepted,
      "is_ai" -> todo.isAi
    ).executeInsert()

    for {
      id <- todoId
    } yield {
      linkTodoToNote (id, noteId)
    }
    todoId
  }

  private def linkTodoToNote(todoId: Long, noteId: Long)(implicit connection: Connection): Int = {
    SQL(
      """
    INSERT INTO note_note_todos (note_id, note_todo_id)
    VALUES ({noteId}, {noteTodoId})
    """).on(
      "noteTodoId" -> todoId,
      "noteId" -> noteId
    ).executeUpdate()
  }

  def findNoteTodosByNoteId(noteId: Long)(implicit connection: Connection): Option[List[NoteTodo]] = {
    val todos = SQL("SELECT * FROM note_todos inner join note_note_todos on id = note_todo_id  WHERE note_id = " +
      "{noteId}").on("noteId" -> noteId).as(NoteTodo.parser.*)
    todos.size match {
      case 0 => None
      case _ => Some(todos)
    }
  }
}


