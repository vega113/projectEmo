package dao

import anorm.SQL
import dao.model.UserInfo

import java.sql.Connection

class UserInfoDao {
  def upsertUserInfo(userId: Long, aiAssistantId: Long, aiThreadId: Long)(implicit connection: Connection): Option[Long] = {
    SQL(
      """
        |INSERT INTO user_info (user_id, ai_assistant_id, thread_id)
        |VALUES ({userId}, {aiAssistantId}, {threadId})
        |ON DUPLICATE KEY UPDATE ai_assistant_id = VALUES(ai_assistant_id), thread_id = VALUES(thread_id)
        |""".stripMargin)
      .on("userId" -> userId, "aiAssistantId" -> aiAssistantId, "threadId" -> aiThreadId)
      .executeInsert()
  }

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
