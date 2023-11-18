package dao

import anorm.{SQL, SqlParser}
import dao.model.Tag

import java.sql.Connection

class TagDao {

  def insert(emotionRecordId: Long, tagName: String)(implicit connection: Connection): Int = {
    if (!checkTagExistsForEmotionRecordByTagName(emotionRecordId, tagName)) {
      val tagId = SQL(
        """
        INSERT INTO tags (name)
        VALUES ({tagName})""").
        on("tagName" -> tagName).
        executeInsert(SqlParser.scalar[Long].single)
      linkTagToEmotionRecord(tagId, emotionRecordId)
    } else {
      0
    }
  }

  def delete(emotionRecordId: Long, tagId: Long)(implicit connection: Connection): Int = {
    if (checkTagExistsForEmotionRecord(emotionRecordId, tagId)) {
      val tagsDeletedCount = SQL("DELETE FROM tags WHERE id = {id}").on("id" -> tagId).executeUpdate()
      val unLinkedCount =  unlinkTagToEmotionRecord(emotionRecordId, tagId)
      if (tagsDeletedCount == 1 && unLinkedCount == 1) {
        1
      } else {
        0
      }
    } else {
      0
    }
  }
  def findAllByEmotionRecordId(id: Long)(implicit connection: Connection): List[Tag] = {
    SQL("SELECT * FROM tags inner join emotion_record_tags on  tag_id = id WHERE emotion_record_id = {id}").on("id" -> id).as(Tag.parser.*)
  }

  def insert(emotionRecordId: Long, tags: Set[Tag])(implicit connection: Connection): Set[Long] = {
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

  private def unlinkTagToEmotionRecord(emotionRecordId: Long, tagId: Long)(implicit connection: Connection): Int = {
    SQL("DELETE FROM emotion_record_tags WHERE emotion_record_id = {emotionRecordId} and tag_id = {tagId}").
      on("emotionRecordId" -> emotionRecordId, "tagId" -> tagId).executeUpdate()
  }

  private def checkTagExistsForEmotionRecord(emotionRecordId: Long, tagId: Long)(implicit connection: Connection): Boolean = {
    SQL("SELECT count(*) FROM tags inner join emotion_record_tags on id = tag_id  WHERE emotion_record_id = " +
      "{emotionRecordId} and tag_id = {tagId}").
      on("emotionRecordId" -> emotionRecordId, "tagId" -> tagId).as(SqlParser.scalar[Int].single) > 0
  }

  private def checkTagExistsForEmotionRecordByTagName(emotionRecordId: Long, tagName: String)(implicit connection: Connection): Boolean = {
    SQL("SELECT count(*) FROM tags inner join emotion_record_tags on id = tag_id  WHERE emotion_record_id = " +
      "{emotionRecordId} and name = {tagName}").
      on("emotionRecordId" -> emotionRecordId, "tagName" -> tagName).as(SqlParser.scalar[Int].single) > 0
  }
}
