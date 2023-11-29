package dao

import anorm.{SQL, SqlParser}
import dao.model.Tag

import java.sql.Connection

class TagDao {
  def deleteByEmotionRecordId(id: Long, userId: Long)(implicit connection: Connection): Int = {
    SQL(
      """
        |DELETE FROM tags
        |WHERE emotion_record_id = {id}
        |AND user_id = {userId}
        |""".stripMargin).on("id" -> id, "userId" -> userId).executeUpdate()
  }


  def delete(userId: Long, tagId: Long)(implicit connection: Connection): Int = {
    val tagsDeletedCount = SQL(
      """
        |DELETE FROM tags WHERE id = {id}
        | and user_id = {userId}
        |""".stripMargin
    )
      .on(
        "id" -> tagId,
        "userId" -> userId
      ).executeUpdate()
    tagsDeletedCount
  }
  def findAllByEmotionRecordId(id: Long)(implicit connection: Connection): List[Tag] = {
    SQL("SELECT * FROM tags WHERE emotion_record_id = {id}").on("id" -> id).as(Tag.parser.*)
  }

  def insert(emotionRecordId: Long, userId: Long, tags: Set[Tag])(implicit connection: Connection): Set[Long] = {
    tags.map { tag =>
      val tagId: Long = SQL(
        """
      INSERT INTO tags (name, user_id, emotion_record_id)
      VALUES ({tagName}, {userId}, {emotionRecordId})"""
      ).
        on("tagName" -> tag.tagName,
          "userId" -> userId,
          "emotionRecordId" -> emotionRecordId
        ).
        executeInsert(SqlParser.scalar[Long].single)
      tagId
    }
  }
}
