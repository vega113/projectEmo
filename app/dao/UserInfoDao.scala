package dao

import anorm.SQL
import dao.AiAssistant.UserInfo

import java.sql.Connection

class UserInfoDao {
  def fetchUserInfo(userId: Long)(implicit connection: Connection): Option[UserInfo] = {
    SQL(
      """
        |SELECT
        |  user_id,
        |  user_name,
        |  user_email,
        |  user_password,
        |  user_created_at,
        |  user_updated_at
        |FROM
        |  user_info
        |WHERE
        |  user_id = {userId}
        |""".stripMargin)
      .on("userId" -> userId)
      .as(UserInfo.parser.singleOpt
    )
  }
}
