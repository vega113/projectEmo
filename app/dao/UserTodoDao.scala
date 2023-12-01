package dao

import anorm.{SQL, SqlParser}
import dao.model.{Note, UserTodo}
import service.DateTimeService

import java.sql.Connection
import javax.inject.Inject

class UserTodoDao @Inject()(dateTimeService: DateTimeService) {
  def delete(userId: Long, userTodoId: Long)(implicit connection: Connection): Int = {
    SQL(
      """
      UPDATE user_todos
      SET is_deleted = true, last_updated = {lastUpdated}
      WHERE id = {id} and user_id = {userId}
      """).on("id" -> userTodoId,
        "userId" -> userId,
        "lastUpdated" -> dateTimeService.now())
      .executeUpdate()
  }

  def fetchByUserIdTodoId(userId: Long, id: Long)(implicit connection: Connection): Option[UserTodo] = {
    SQL("SELECT * FROM user_todos WHERE user_id = {userId} and id = {id} and is_deleted = false")
      .on("userId" -> userId, "id" -> id)
      .as(UserTodo.parser.singleOpt)
  }

  def fetchByUserId(userId: Long)(implicit connection: Connection): List[UserTodo] = {
    SQL("SELECT * FROM user_todos WHERE user_id = {userId} and is_deleted <> true order by last_updated desc")
      .on("userId" -> userId)
      .as(UserTodo.parser.*)
  }

  def update(todo: UserTodo)(implicit connection: Connection): Int = {
    SQL(
      """
      UPDATE user_todos
      SET title = {title}, description = {description}, is_deleted = {isDeleted}, last_updated = {lastUpdated}
      WHERE id = {id} and user_id = {userId}
      """).on("id" -> todo.id,
        "userId" -> todo.userId,
        "title" -> todo.title,
        "description" -> todo.description,
        "isDeleted" -> todo.isDeleted,
        "lastUpdated" -> dateTimeService.now()).executeUpdate()
  }

  def findAllNotDeletedByEmotionRecordId(id: Long)(implicit connection: Connection): List[UserTodo] = {
    SQL("SELECT * FROM user_todos inner join emotion_record_todos on id = todo_id  WHERE emotion_record_id = " +
      "{id} and is_deleted = false").on("id" -> id).as(UserTodo.parser.*)
  }


  def insert(todo: UserTodo)(implicit connection: Connection): Option[Long] = {
    val todoId: Option[Long] = SQL(
      """
    INSERT INTO user_todos (user_id, title, description, color, is_done, is_archived, is_deleted, is_ai, is_read)
    VALUES ({userId}, {title}, {description}, {color}, {isDone}, {isArchived}, {isDeleted}, {isAi}, {isRead})
  """).on(
      "userId" -> todo.userId,
      "title" -> todo.title,
      "description" -> todo.description,
      "color" -> todo.color,
      "isDone" -> todo.isDone,
      "isArchived" -> todo.isArchived,
      "isDeleted" -> todo.isDeleted,
      "isAi" -> todo.isAi,
      "isRead" -> todo.isRead
    ).executeInsert()
    todoId
  }

  def archive(userId: Long, userTodoId: Long)(implicit connection: Connection): Int = {
    SQL(
      """
      UPDATE user_todos
      SET is_archived = true, last_updated = {lastUpdated}
      WHERE id = {id} and user_id = {userId}
      """).on("id" -> userTodoId,
        "userId" -> userId,
        "lastUpdated" -> dateTimeService.now())
      .executeUpdate()
  }

  def unarchive(userId: Long, userTodoId: Long)(implicit connection: Connection): Int = {
    SQL(
      """
      UPDATE user_todos
      SET is_archived = false, last_updated = {lastUpdated}
      WHERE id = {id} and user_id = {userId}
      """).on("id" -> userTodoId,
        "userId" -> userId,
        "lastUpdated" -> dateTimeService.now())
      .executeUpdate()
  }

  def complete(userId: Long, userTodoId: Long)(implicit connection: Connection): Int = {
    SQL(
      """
      UPDATE user_todos
      SET is_done = true, last_updated = {lastUpdated}
      WHERE id = {id} and user_id = {userId}
      """).on("id" -> userTodoId,
        "userId" -> userId,
        "lastUpdated" -> dateTimeService.now())
      .executeUpdate()
  }

  def uncomplete(userId: Long, userTodoId: Long)(implicit connection: Connection): Int = {
    SQL(
      """
      UPDATE user_todos
      SET is_done = false, last_updated = {lastUpdated}
      WHERE id = {id} and user_id = {userId}
      """).on("id" -> userTodoId,
        "userId" -> userId,
        "lastUpdated" -> dateTimeService.now())
      .executeUpdate()
  }

  def findEmotionRecordIdByTodoId(todoId: Long)(implicit connection: Connection): Option[Long] = {
    SQL("SELECT emotion_record_id FROM emotion_record_user_todos WHERE user_todo_id = {todoId}")
      .on("todoId" -> todoId)
      .as(SqlParser.scalar[Long].singleOpt)
  }
}


