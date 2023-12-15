package dao

import anorm.SQL
import dao.AiAssistant.UserInfo

import java.sql.Connection

class UserInfoDao {
  def fetchUserInfo(userId: Long)(implicit connection: Connection): Option[UserInfo] = {
    SQL(
      """
        |SELECT * FROM user_info
        |WHERE user_id = {userId}
        |""".stripMargin)
      .on("userId" -> userId)
      .as(UserInfo.parser.singleOpt
    )
  }
}
