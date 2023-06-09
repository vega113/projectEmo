package dao
import anorm.SQL
import dao.model.Trigger

import java.sql.Connection
class EmotionRecordTriggerDao {

  def findAllTriggersByEmotionRecordId(emotionRecordId: Long)(implicit connection: Connection): List[Trigger] = {
    SQL(
      """
        |SELECT t.*
        |FROM triggers t
        |JOIN emotion_record_triggers ert ON t.trigger_id = ert.parent_trigger_id
        |WHERE ert.parent_emotion_record_id = {emotionRecordId}
        |""".stripMargin).on("emotionRecordId" -> emotionRecordId).as(Trigger.parser.*)
  }

  def deleteByEmotionRecordId(emotionRecordId: Long)(implicit connection: Connection): Int = {
    SQL("DELETE FROM emotion_record_triggers " +
      "WHERE parent_emotion_record_id = {emotionRecordId}").on("emotionRecordId" -> emotionRecordId).executeUpdate()
  }
}
