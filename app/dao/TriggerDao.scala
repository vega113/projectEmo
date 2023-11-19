package dao

import anorm._
import dao.model._

import java.sql.Connection

class TriggerDao {
  def findAll()(implicit connection: Connection): List[Trigger] = {
    SQL("SELECT * FROM triggers").as(Trigger.parser.*)
  }

  def findById(id: Int)(implicit connection: Connection): Option[Trigger] = {
    SQL("SELECT * FROM triggers WHERE id = {id}").on("id" -> id).as(Trigger.parser.singleOpt)
  }

  def insert(trigger: Trigger, emotionRecordId: Long)(implicit connection: Connection): Option[Long] = {
    val triggerId: Option[Long] = SQL("INSERT INTO triggers (user_id, description) VALUES ({userId}, {description})")
      .on("userId" -> trigger.createdByUser, "description" -> trigger.description)
      .executeInsert()
    triggerId match {
      case Some(id) => linkTriggerToEmotionRecord(id, emotionRecordId)
    }
    triggerId
  }

  def update(trigger: Trigger)(implicit connection: Connection): Int = {
    SQL("UPDATE triggers SET user_id = {userId}, description = {description} WHERE id = {id}")
      .on("id" -> trigger.triggerId, "userId" -> trigger.createdByUser, "description" -> trigger.description)
      .executeUpdate()
  }

  def delete(id: Int)(implicit connection: Connection): Int = {
    SQL("DELETE FROM triggers WHERE id = {id}").on("id" -> id).executeUpdate()
  }

  def linkTriggerToEmotionRecord(triggerId: Long, emotionRecordId: Long)(implicit connection: Connection): Long = {
    SQL(
      """
      INSERT INTO emotion_record_triggers (parent_trigger_id, parent_emotion_record_id)
      VALUES ({triggerId}, {emotionRecordId})""").
      on("triggerId" -> triggerId, "emotionRecordId" -> emotionRecordId).executeUpdate()
  }
}
