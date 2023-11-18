package service

import com.google.inject.ImplementedBy
import dao.model.{NoteTodo, UserTodo}
import dao.{DatabaseExecutionContext, NoteTodoDao, UserTodoDao}
import net.logstash.logback.argument.StructuredArguments._
import org.slf4j.{Logger, LoggerFactory}

import java.sql.Connection
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[NoteTodoServiceImpl])
trait NoteTodoService {
  def insert(text: String, noteId: Long, todo: NoteTodo): Future[Option[Long]]

  def extractTodos(text: String): List[NoteTodo]

  def acceptNoteTodo(userId: Long, noteTodoId: Long): Future[Boolean]

  def fetchById(id: Long): Future[Option[NoteTodo]]
}

class NoteTodoServiceImpl @Inject()(databaseExecutionContext: DatabaseExecutionContext,
                                    noteTodoDao: NoteTodoDao,
                                    userTodoDao: UserTodoDao,
                                    titleService: TitleService) extends NoteTodoService {
  private val logger: Logger = LoggerFactory.getLogger(classOf[NoteTodoServiceImpl])

  override def insert(text: String, noteId: Long, todo: NoteTodo): Future[Option[Long]] = {
    logger.info("Inserting note todo, noteId: {}", value("noteId", noteId))
    databaseExecutionContext.withConnection({ implicit connection =>
      val todoId: Long = noteTodoDao.insert(noteId, todo) match {
        case Some(id) =>
          logger.info(s"Inserted note todo {} {}:", value("noteId", noteId), value("todo", todo))
          id
        case None =>
          val errorMsg = s"Failed to insert note todo: {}"
          logger.error(errorMsg, Map("noteId" -> noteId, "todo" -> todo))
          throw new Exception(errorMsg)
      }
      Future.successful(Some(todoId))
    })
  }

  def acceptNoteTodo(userId: Long, noteTodoId: Long): Future[Boolean] = {
    def logAcceptedStatus(updated: Boolean): Unit = {
      if (updated) {
        logger.info(s"Updated note todos: {}", value("noteId", noteTodoId))
      } else {
        logger.error(s"Failed to update note todos: {}", value("noteId", noteTodoId))
      }
    }

    def createUserTodoFromNoteTodo(userId: Long, noteTodoId: Long)(implicit connection: Connection): Unit = {
      val noteTodo = noteTodoDao.findById(noteTodoId)
      noteTodo match {
        case Some(todo) =>
          val userNoteTodo = UserTodo(None, Option(userId), todo.title, Option(todo.description), None,
            isDone = false,
            isArchived = false, isDeleted = false, isAi = todo.isAi, isRead = false, None, None)
          userTodoDao.insert(userNoteTodo.copy(userId = Option(userId))) match {
            case Some(id) =>
              logger.info(s"Inserted user todo: {}", value("userTodo", userNoteTodo.copy(id = Some(id))))
            case None =>
              val errorMsg = s"Failed to insert user todo: {}"
              logger.error(errorMsg, Map("userTodo" -> userNoteTodo))
              throw new Exception(errorMsg)
          }
          logger.info(s"Inserted user todo: {}", value("userTodo", userNoteTodo))
      }
    }

    Future {
      databaseExecutionContext.withConnection({ implicit connection =>
        val isNotesBelongsToUser = noteTodoDao.verifyNoteTodoBelongsToUser(userId, noteTodoId)
        if (isNotesBelongsToUser) {
          val updated: Boolean = noteTodoDao.acceptNoteTodo(noteTodoId)
          logAcceptedStatus(updated)
          if(updated) {
            createUserTodoFromNoteTodo(userId, noteTodoId)
          }
          updated
        }
        else {
          logger.error(s"Note todo does not belong to user: {}", value("noteId", noteTodoId))
          false
        }
      })
    }
  }
override def extractTodos(text: String): List[NoteTodo] = {
  val todoRegex = "(?s)(?<=\\[\\[).+?(?=]])".r
  todoRegex.findAllIn(text).toList.map(todo => NoteTodo(None, titleService.makeTitle(todo), todo, isAccepted = false,
    isAi = false))
}

  override def fetchById(id: Long): Future[Option[NoteTodo]] = {
    Future {
      databaseExecutionContext.withConnection({ implicit connection =>
        noteTodoDao.findById(id)
      })
    }
  }
}
