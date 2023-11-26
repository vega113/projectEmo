package service

import com.google.inject.ImplementedBy
import dao.model.UserTodo
import dao.{DatabaseExecutionContext, UserTodoDao}
import org.slf4j.{Logger, LoggerFactory}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[UserTodoServiceImpl])
trait UserTodoService {
  def delete(userId: Long, userTodoId: Long): Future[List[UserTodo]]

  def fetchByUserId(userId: Long):Future[List[UserTodo]]

  def insert(emotionRecordId: Option[Long], noteId: Option[Long], todo: UserTodo): Future[List[UserTodo]]

  def update(todo: UserTodo): Future[List[UserTodo]]
  def findEmotionRecordIdByTodoId(todoId: Long): Future[Option[Long]]
  def archive(userId: Long, id: Long): Future[List[UserTodo]]
  def unarchive(userId: Long, id: Long): Future[List[UserTodo]]

  def complete(userId: Long, id: Long): Future[List[UserTodo]]
  def uncomplete(userId: Long, id: Long): Future[List[UserTodo]]
}
class UserTodoServiceImpl @Inject()(databaseExecutionContext: DatabaseExecutionContext,
                                    todoDao: UserTodoDao) extends UserTodoService {

  override def insert(emotionRecordId: Option[Long], noteId: Option[Long], todo: UserTodo): Future[List[UserTodo]] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      if (todo.userId.isDefined &&  todoDao.insert(todo).isDefined) {
        Future.successful(todoDao.fetchByUserId(todo.userId.get))
      } else {
        Future.failed(new Exception("Failed to insert todo"))
      }
    })
  }

  def archive(userId: Long, id: Long): Future[List[UserTodo]] = {
    Future {
      databaseExecutionContext.withConnection { implicit connection =>
        todoDao.fetchByUserIdTodoId(userId, id) match {
          case Some(todo) if !todo.isDone =>
            if (todoDao.archive(userId, id) > 0)
              todoDao.fetchByUserId(userId)
            else
              throw new Exception("Failed to archive todo")
          case _ => throw new Exception("Todo not found or already completed")
        }
      }
    }
  }


  override def unarchive(userId: Long, id: Long): Future[List[UserTodo]] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      if (todoDao.unarchive(userId, id) > 0) {
        Future.successful(todoDao.fetchByUserId(userId))
      } else {
        Future.failed(new Exception("Failed to unarchive todo"))
      }
    })
  }

  override def complete(userId: Long, id: Long): Future[List[UserTodo]] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      if (todoDao.complete(userId, id) > 0) {
        Future.successful(todoDao.fetchByUserId(userId))
      } else {
        Future.failed(new Exception("Failed to complete todo"))
      }
    })
  }

  override def uncomplete(userId: Long, id: Long): Future[List[UserTodo]] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      if (todoDao.uncomplete(userId, id) > 0) {
        Future.successful(todoDao.fetchByUserId(userId))
      } else {
        Future.failed(new Exception("Failed to uncomplete todo"))
      }
    })
  }

  override def update(todo: UserTodo): Future[List[UserTodo]] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      if (todoDao.update(todo) > 0) {
        todo.userId match {
          case Some(userId) => Future.successful(todoDao.fetchByUserId(userId))
          case None => Future.failed(new Exception("Failed to update todo"))
        }
      } else {
        Future.failed(new Exception("Failed to update todo"))
      }
    })
  }

  override def fetchByUserId(userId: Long): Future[List[UserTodo]] = {
    Future(databaseExecutionContext.withConnection({ implicit connection =>
      todoDao.fetchByUserId(userId)
    }))
  }

  override def findEmotionRecordIdByTodoId(todoId: Long): Future[Option[Long]] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      Future.successful(todoDao.findEmotionRecordIdByTodoId(todoId))
    })
  }

  override def delete(userId: Long, userTodoId: Long): Future[List[UserTodo]] =
    databaseExecutionContext.withConnection({ implicit connection =>
      if (todoDao.delete(userId, userTodoId) > 0) {
        Future.successful(todoDao.fetchByUserId(userId))
      } else {
        Future.failed(new Exception("Failed to delete todo"))
      }
    })
}
