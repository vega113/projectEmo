package service

import com.google.inject.Inject
import dao.AiAssistant.UserInfo
import dao.{DatabaseExecutionContext, UserInfoDao}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait UserInfoService {
  def fetchUserInfo(userId: Long): Future[Option[UserInfo]]
}

class UserInfoServiceImpl @Inject()(userInfoDao: UserInfoDao, databaseExecutionContext: DatabaseExecutionContext) extends UserInfoService {

  override def fetchUserInfo(userId: Long): Future[Option[UserInfo]] = {
    Future(databaseExecutionContext.withConnection({ implicit connection =>
      userInfoDao.fetchUserInfo(userId)
    }))
  }
}
