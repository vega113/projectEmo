package dao

import anorm._
import dao.model._

import java.sql.Connection
import javax.inject.Inject

class EmotionRecordDao @Inject()(emotionRecordSubEmotionDao: EmotionRecordSubEmotionDao, emotionRecordTriggerDao: EmotionRecordTriggerDao) {
  def findAll()(implicit connection: Connection): List[EmotionRecord] = {
    val emotionRecords = SQL("SELECT * FROM emotion_records").as(EmotionRecord.parser.*)
    populateSubEmotionsTriggersByEmotionRecord(emotionRecords)
  }

  private def populateSubEmotionsTriggersByEmotionRecord(emotionRecords: List[EmotionRecord])(implicit connection: Connection) = {
    for {
      emotionRecord <- emotionRecords
      id <- emotionRecord.id.toList
    } yield emotionRecord.copy(
      subEmotions = emotionRecordSubEmotionDao.findAllSubEmotionsByEmotionRecordId(id),
      triggers = emotionRecordTriggerDao.findAllTriggersByEmotionRecordId(id)
    )
  }

  def findById(id: Long)(implicit connection: Connection): Option[EmotionRecord] = {
    val emotionRecords = SQL("SELECT * FROM emotion_records WHERE id = {id}").on("id" -> id).
      as(EmotionRecord.parser.singleOpt)
    populateSubEmotionsTriggersByEmotionRecord(List(emotionRecords).flatten).headOption
  }

  def findAllByUserId(userId: Long)(implicit connection: Connection): List[EmotionRecord] = {
    val emotionRecords = SQL("SELECT * FROM emotion_records WHERE user_id = {userId}").on("userId" -> userId).
      as(EmotionRecord.parser.*)
    populateSubEmotionsTriggersByEmotionRecord(emotionRecords)
  }

  def insert(emotionRecord: EmotionRecord)(implicit connection: Connection): Option[Long] = {
    val idOpt: Option[Long] = SQL(
      """
      INSERT INTO emotion_records (emotion_id, user_id, intensity)
      VALUES ({emotionId}, {userId}, {intensity})
    """).on("userId" -> emotionRecord.userId,
      "emotionId" -> emotionRecord.emotionId,
      "intensity" -> emotionRecord.intensity)
      .executeInsert()
    idOpt.foreach(id => insertSubLists(emotionRecord, id))
    idOpt
  }

  private def insertSubLists(emotionRecord: EmotionRecord, id: Long)(implicit connection: Connection) = {
    for {
      subEmotion <- emotionRecord.subEmotions
    } yield {
      SQL(
        """
      INSERT INTO emotion_record_sub_emotions(parent_emotion_record_id, parent_sub_emotion_id)
      VALUES ({id}, {subEmotionId})
      """).on("subEmotionId" -> subEmotion.subEmotionId,
        "id" -> id)
        .executeInsert()
    }

    for {
      trigger <- emotionRecord.triggers
    } yield {
      SQL(
        """
      INSERT INTO emotion_record_triggers(parent_emotion_record_id, parent_trigger_id)
      VALUES ({id}, {triggerId})
      """).on("triggerId" -> trigger.triggerId,
        "id" -> id)
        .executeInsert()
    }
  }

  def update(emotionRecord: EmotionRecord)(implicit connection: Connection): Int = {
    val insertedCount = SQL(
      """
      UPDATE emotion_records
      SET emotion_id = {emotionId}, intensity = {intensity}
      WHERE id = {id}
    """).on("id" -> emotionRecord.id,
      "userId" -> emotionRecord.userId,
      "emotionId" -> emotionRecord.emotionId,
      "intensity" -> emotionRecord.intensity)
      .executeUpdate()

    emotionRecord.id.foreach(id => {
      emotionRecordSubEmotionDao.deleteByEmotionRecordId(id)
      emotionRecordTriggerDao.deleteByEmotionRecordId(id)
      insertSubLists(emotionRecord, id)
    })
    insertedCount
  }

  def delete(id: Long)(implicit connection: Connection): Int = {
    SQL("DELETE FROM emotion_records WHERE id = {id}").on("id" -> id).executeUpdate()
  }
}
