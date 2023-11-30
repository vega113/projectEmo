package dao
import anorm.SQL
import dao.model.Trigger

import java.sql.Connection
class EmotionRecordTriggerDao {

  def findAllTriggersByEmotionRecordId(emotionRecordId: Long)(implicit connection: Connection): List[Trigger] = {
    SQL(
      """
        |SELECT tr.*
        |FROM emotion_records er
        |JOIN triggers tr ON er.trigger_id = tr.trigger_id
        |WHERE er.id = {emotionRecordId}
        |""".stripMargin).on("emotionRecordId" -> emotionRecordId).as(Trigger.parser.*)
  }
}
