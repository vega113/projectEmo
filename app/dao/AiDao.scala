package dao

import anorm.SQL
import dao.model.AiDbObj

import java.sql.Connection

class AiDao {
  def insert(response: AiDbObj)(implicit connection: Connection): Option[Long] = {
    SQL(
      """
        |INSERT INTO ai_responses (response, user_id)
        |VALUES ({response}, {userId})
        |""".stripMargin)
      .on(
        "response" -> response.response, "userId" -> response.userId
      ).executeInsert()
  }
}
