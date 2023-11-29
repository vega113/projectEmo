package service

import com.google.inject.ImplementedBy
import dao.model.{Note, NoteTodo, UserTodo}
import dao.{DatabaseExecutionContext, NoteTodoDao, UserTodoDao}
import net.logstash.logback.argument.StructuredArguments._
import org.slf4j.{Logger, LoggerFactory}

import java.sql.Connection
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

@ImplementedBy(classOf[NoteTodoServiceImpl])
trait NoteTodoService {
  def insert(todo: NoteTodo): Future[Option[Long]]

  def extractTodos(note: Note): List[NoteTodo]

  def acceptNoteTodo(userId: Long, noteTodoId: Long): Future[Boolean]

  def fetchById(id: Long): Future[Option[NoteTodo]]
}

class NoteTodoServiceImpl @Inject()(databaseExecutionContext: DatabaseExecutionContext,
                                    noteTodoDao: NoteTodoDao,
                                    userTodoDao: UserTodoDao,
                                    titleService: TitleService) extends NoteTodoService {
  private val logger: Logger = LoggerFactory.getLogger(classOf[NoteTodoServiceImpl])

  override def insert(todo: NoteTodo): Future[Option[Long]] = {
    logger.info("Inserting note todo, userId: {}", value("userId", todo.userId))
    databaseExecutionContext.withConnection({ implicit connection =>
      val todoId: Long = noteTodoDao.insert(todo) match {
        case Some(id) =>
          logger.info(s"Inserted note todo {} {}:", value("noteId", todo.noteId), value("todo", todo))
          id
        case None =>
          val errorMsg = s"Failed to insert note todo: {}"
          logger.error(errorMsg, Map("noteId" -> todo.noteId, "todo" -> todo))
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

    def createUserTodoFromNoteTodo(todo: NoteTodo, noteTodoId: Long)(implicit connection: Connection): Boolean = {

      val userNoteTodo = UserTodo(None, Option(userId), todo.title, Option(todo.description), None,
        isDone = false,
        isArchived = false, isDeleted = false, isAi = todo.isAi, isRead = Some(false), Some(noteTodoId), None, None)
      userTodoDao.insert(userNoteTodo.copy(userId = Option(userId))) match {
        case Some(id) =>
          logger.info(s"Inserted user todo: {}", value("userTodo", userNoteTodo.copy(id = Some(id))))
          id > 0
        case None =>
          val errorMsg = s"Failed to insert user todo: {}"
          logger.error(errorMsg, Map("userTodo" -> userNoteTodo))
          throw new Exception(errorMsg)
      }
    }

    Future {
      databaseExecutionContext.withConnection({ implicit connection =>
        noteTodoDao.findById(noteTodoId) match {
          case Some(noteTodo) =>
            val updated: Boolean = noteTodoDao.acceptNoteTodo(noteTodo.userId.
              getOrElse(throw new Exception("userId is None")), noteTodoId)
            logAcceptedStatus(updated)
            if (updated) {
              createUserTodoFromNoteTodo(noteTodo, noteTodoId)
            }
            updated
          case None =>
            logger.error(s"Failed to find note todo: {}", value("noteId", noteTodoId))
            false
        }
      })
    }
  }
override def extractTodos(note: Note): List[NoteTodo] = {
  val todoRegex = "(?s)(?<=\\[\\[).+?(?=]])".r
  todoRegex.findAllIn(note.text).toList.map(todo => NoteTodo(None, titleService.makeTitle(todo), todo, isAccepted = Some(false),
    isAi = Some(false), noteId = note.id, userId = note.userId, emotionRecordId = note.emotionRecordId
  )).filter(_.description.nonEmpty)
}

  override def fetchById(id: Long): Future[Option[NoteTodo]] = {
    Future {
      databaseExecutionContext.withConnection({ implicit connection =>
        noteTodoDao.findById(id)
      })
    }
  }
}
