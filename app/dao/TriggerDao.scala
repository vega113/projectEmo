package dao

import anorm._
import dao.model._

import java.sql.Connection

class TriggerDao {
  def deleteByEmotionRecordId(id: Long, userId: Long)(implicit connection: Connection): Int = {
    SQL(
      """
        |DELETE FROM emotion_record_triggers
        |WHERE parent_emotion_record_id = {id}
        |AND user_id = {userId}
        |""".stripMargin).on("id" -> id, "userId" -> userId).executeUpdate()
  }

  def findAll()(implicit connection: Connection): List[Trigger] = {
    SQL("SELECT * FROM triggers").as(Trigger.parser.*)
  }

  def findById(id: Int)(implicit connection: Connection): Option[Trigger] = {
    SQL("SELECT * FROM triggers WHERE id = {id}").on("id" -> id).as(Trigger.parser.singleOpt)
  }

  def insert(trigger: Trigger, triggerParentId: Long)(implicit connection: Connection): Option[Long] = {
    val triggerId: Option[Long] = SQL(
      """
        |INSERT INTO triggers (trigger_id, trigger_name, created_by_user, description, trigger_parent_id)
        |VALUES ({triggerId}, {triggerName},  {createdByUser}, {description}, {triggerParentId})
        |""".stripMargin).on("triggerId" -> trigger.triggerId, "triggerName" -> trigger.triggerName,
        "createdByUser" -> trigger.createdByUser, "description" -> trigger.description,
      "triggerParentId" -> triggerParentId
      ).executeInsert()
    triggerId
  }

  def update(trigger: Trigger)(implicit connection: Connection): Int = {
    SQL(
      """
        |UPDATE triggers
        |SET  trigger_name = {triggerName}, created_by_user = {createdByUser},
        |description = {description}, trigger_parent_id = {triggerParentId}
        |WHERE trigger_id = {triggerId}
        |""".stripMargin).on("triggerId" -> trigger.triggerId, "triggerName" -> trigger.triggerName,
      "createdByUser" -> trigger.createdByUser, "description" -> trigger.description,
      "triggerParentId" -> trigger.parentId
    ).executeUpdate()
  }

  def delete(id: Int)(implicit connection: Connection): Int = {
    SQL("DELETE FROM triggers WHERE trigger_id = {id}").on("id" -> id).executeUpdate()
  }

  def linkTriggerToEmotionRecord(triggerId: Long, emotionRecordId: Long)(implicit connection: Connection): Long = {
    SQL(
      """
        |UPDATE emotion_records
        |SET trigger_id = {triggerId}
        |WHERE id = {emotionRecordId}
        |""".stripMargin).on("triggerId" -> triggerId, "emotionRecordId" -> emotionRecordId).executeUpdate()
  }

  def findByName(name: String)(implicit connection: Connection): Trigger = {
    SQL("SELECT * FROM triggers WHERE trigger_name = {triggerName}").on("triggerName" -> name).
      as(Trigger.parser.single)
  }
}
