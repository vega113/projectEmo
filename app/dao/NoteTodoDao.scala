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

  def acceptNoteTodo(userId: Long, noteTodoId: Long)(implicit connection: Connection): Boolean = {
    val updated: Int = SQL(
      """
    UPDATE note_todos
    SET is_accepted = true
    WHERE id = {noteTodoId} and user_id = {userId}
    """).on(
      "noteTodoId" -> noteTodoId, "userId" -> userId
    ).executeUpdate()
    updated == 1
  }

  def insert(todo: NoteTodo)(implicit connection: Connection): Option[Long] = {
    val todoId: Option[Long] = SQL("""
  INSERT INTO note_todos (title, description, is_accepted, user_id, emotion_record_id, note_id, is_ai)
  VALUES ({title}, {description}, {is_accepted}, {userId}, {emotionRecordId}, {noteId}, {is_ai})
""").on(
      "title" -> todo.title,
      "description" -> todo.description,
      "is_accepted" -> todo.isAccepted,
      "userId" -> todo.userId,
      "emotionRecordId" -> todo.emotionRecordId,
      "noteId" -> todo.noteId,
      "is_ai" -> todo.isAi
    ).executeInsert()
    todoId
  }

  def findNoteTodosByNoteId(noteId: Long)(implicit connection: Connection): Option[List[NoteTodo]] = {
    val todos = SQL("SELECT * FROM note_todos  WHERE note_id = " +
      "{noteId}").on("noteId" -> noteId).as(NoteTodo.parser.*)
    todos.size match {
      case 0 => None
      case _ => Some(todos)
    }
  }
}


