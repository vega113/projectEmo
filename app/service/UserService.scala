package service

import com.google.inject.ImplementedBy
import dao.model.User
import dao.{DatabaseExecutionContext, UserDao}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[UserServiceImpl])
trait UserService {
  def findByUsername(username: String): Future[Option[User]]

  def findById(userId: Long): Future[Option[User]]

  def findByEmail(email: String): Future[Option[User]]

  def findAll(): Future[List[User]]

  def insert(user: User): Future[Option[Long]]

  def update(user: User): Future[Int]

  def delete(userId: Long): Future[Int]
}

class UserServiceImpl @Inject()(userDao: UserDao, dbExecutionContext: DatabaseExecutionContext) extends UserService {
  override def findByUsername(username: String): Future[Option[User]] = {
    Future(dbExecutionContext.withConnection { implicit connection =>
      userDao.findByUsername(username)
    })
  }

  override def findById(userId: Long): Future[Option[User]] = {
    Future(dbExecutionContext.withConnection { implicit connection =>
      userDao.findById(userId)
    })
  }

  override def findByEmail(email: String): Future[Option[User]] = {
    Future(dbExecutionContext.withConnection { implicit connection =>
      userDao.findByEmail(email)
    })
  }

  override def findAll(): Future[List[User]] = {
    Future(dbExecutionContext.withConnection { implicit connection =>
      userDao.findAll()
    })
  }

  override def insert(user: User): Future[Option[Long]] = {
    Future(dbExecutionContext.withConnection { implicit connection =>
      if (userDao.checkUserExists(user.username, user.email)) {
        None
      } else {
        userDao.insert(user)
      }
    })
  }

  override def update(user: User): Future[Int] = {
    Future(dbExecutionContext.withConnection { implicit connection =>
      userDao.update(user)
    })
  }

  override def delete(userId: Long): Future[Int] = {
    Future(dbExecutionContext.withConnection { implicit connection =>
      userDao.delete(userId)
    })
  }
}
