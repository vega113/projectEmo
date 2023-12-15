package service

import com.google.inject.{ImplementedBy, Inject}
import dao.AiAssistant.UserInfo
import dao.{DatabaseExecutionContext, UserInfoDao}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[UserInfoServiceImpl])
trait UserInfoService {
  def fetchUserInfo(userId: Long): Future[Option[UserInfo]]
  def upsertUserInfo(userId: Long, aiAssistantId: Long, aiThreadId: Long): Future[Option[Long]]
}

class UserInfoServiceImpl @Inject()(userInfoDao: UserInfoDao, databaseExecutionContext: DatabaseExecutionContext) extends UserInfoService {

  override def fetchUserInfo(userId: Long): Future[Option[UserInfo]] = {
    Future(databaseExecutionContext.withConnection({ implicit connection =>
      userInfoDao.fetchUserInfo(userId)
    }))
  }

  override def upsertUserInfo(userId: Long, aiAssistantId: Long, aiThreadId: Long): Future[Option[Long]] = {
    Future(databaseExecutionContext.withConnection({ implicit connection =>
      userInfoDao.upsertUserInfo(userId, aiAssistantId, aiThreadId)
    }))
  }
}
