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
  def fetchByUserId(userId: Long):Future[List[UserTodo]]

  def insert(emotionRecordId: Long, text: String, noteId: Option[Long], todo: UserTodo): Future[Option[Long]]

  def update(todo: UserTodo): Future[Boolean]
  def findEmotionRecordIdByTodoId(todoId: Long): Future[Option[Long]]
  def archive(userId: Long, id: Long): Future[List[UserTodo]]
  def unarchive(userId: Long, id: Long): Future[List[UserTodo]]

  def complete(userId: Long, id: Long): Future[List[UserTodo]]
  def uncomplete(userId: Long, id: Long): Future[List[UserTodo]]
}
class UserTodoServiceImpl @Inject()(databaseExecutionContext: DatabaseExecutionContext,
                                    todoDao: UserTodoDao) extends UserTodoService {

  private val logger: Logger = LoggerFactory.getLogger(classOf[UserTodoServiceImpl])

  override def insert(emotionRecordId: Long, text: String, noteId: Option[Long], todo: UserTodo): Future[Option[Long]] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      val todoId: Long = todoDao.insert(todo) match {
        case Some(id) =>
          id
        case None => throw new Exception("Failed to insert todo")
      }
      Future.successful(Some(todoId))
    })
  }

  override def archive(userId: Long, id: Long): Future[List[UserTodo]] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      if (todoDao.archive(userId, id) > 0) {
        Future.successful(todoDao.fetchByUserId(userId))
      } else {
        Future.failed(new Exception("Failed to delete todo"))
      }
    })
  }

  override def unarchive(userId: Long, id: Long): Future[List[UserTodo]] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      if (todoDao.unarchive(userId, id) > 0) {
        Future.successful(todoDao.fetchByUserId(userId))
      } else {
        Future.failed(new Exception("Failed to archive todo"))
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

  override def update(todo: UserTodo): Future[Boolean] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      Future.successful(todoDao.update(todo) > 0)
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
}
