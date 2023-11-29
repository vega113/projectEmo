package dao

import anorm.SQL
import dao.model.SubEmotion

import java.sql.Connection

class EmotionRecordSubEmotionDao {
  def findAllSubEmotionsByEmotionRecordId(emotionRecordId: Long)(implicit connection: Connection): List[SubEmotion] = {
  SQL(
    """
      |SELECT se.*
      |FROM emotion_records er
      |JOIN sub_emotions se ON er.sub_emotion_id = se.sub_emotion_id
      |WHERE er.id = {emotionRecordId}
      |""".stripMargin).on("emotionRecordId" -> emotionRecordId).as(SubEmotion.parser.*)
  }

  def deleteByEmotionRecordId(emotionRecordId: Long)(implicit connection: Connection): Int = {
    SQL("DELETE FROM emotion_record_sub_emotions " +
      "WHERE parent_emotion_record_id = {emotionRecordId}").on("emotionRecordId" -> emotionRecordId).executeUpdate()
  }
}
