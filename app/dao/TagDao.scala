package dao

import anorm.{SQL, SqlParser}
import dao.model.Tag

import java.sql.Connection

class TagDao {
  def findAllByEmotionRecordId(id: Long)(implicit connection: Connection): List[Tag] = {
    SQL("SELECT * FROM tags inner join emotion_record_tags on  tag_id = id WHERE emotion_record_id = {id}").on("id" -> id).as(Tag.parser.*)
  }

  def insert(emotionRecordId: Long, tags: List[Tag])(implicit connection: Connection): List[Long] = {
    tags.map { tag =>
      val tagId: Long = SQL(
        """
        INSERT INTO tags (name)
        VALUES ({tagName})""").
        on("tagName" -> tag.tagName).
        executeInsert(SqlParser.scalar[Long].single)
      linkTagToEmotionRecord(tagId, emotionRecordId)
      tagId
    }
  }

  private def linkTagToEmotionRecord(tagId: Long, emotionRecordId: Long)(implicit connection: Connection): Int = {
    SQL(
      """
      INSERT INTO emotion_record_tags (tag_id, emotion_record_id)
      VALUES ({tagId}, {emotionRecordId})""").
      on("tagId" -> tagId, "emotionRecordId" -> emotionRecordId).executeUpdate()
  }
}
