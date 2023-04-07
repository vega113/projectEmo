package dao

import anorm.SQL
import dao.model.SubEmotion

import java.sql.Connection

class EmotionRecordSubEmotionDao {
  def findAllSubEmotionsByEmotionRecordId(emotionRecordId: Long)(implicit connection: Connection): List[SubEmotion] = {
  SQL(
    """
      |SELECT s.*
      |FROM sub_emotions s
      |JOIN emotion_record_sub_emotions erse ON s.sub_emotion_id = erse.parent_sub_emotion_id
      |WHERE erse.parent_emotion_record_id = {emotionRecordId}
      |""".stripMargin).on("emotionRecordId" -> emotionRecordId).as(SubEmotion.parser.*)
  }

  def deleteByEmotionRecordId(emotionRecordId: Long)(implicit connection: Connection): Int = {
    SQL("DELETE FROM emotion_record_sub_emotions " +
      "WHERE parent_emotion_record_id = {emotionRecordId}").on("emotionRecordId" -> emotionRecordId).executeUpdate()
  }
}
