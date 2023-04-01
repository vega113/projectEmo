package dao

import anorm._
import dao.model._
import java.sql.Connection

class EmotionRecordWithRelationsDao {
  import EmotionRecordWithRelations.parser

  def findAll()(implicit connection: Connection): List[EmotionRecordWithRelations] = {
    SQL("""
    SELECT er.*, se.*, t.*
    FROM emotion_records er
    LEFT JOIN emotion_records_sub_emotions ers ON er.id = ers.emotion_record_id
    LEFT JOIN sub_emotions se ON ers.sub_emotion_id = se.id
    LEFT JOIN emotion_record_triggers ert ON er.id = ert.emotion_record_id
    LEFT JOIN triggers t ON ert.trigger_id = t.id
  """).as(parser.*)
  }

  def findById(id: Int)(implicit connection: Connection): Option[EmotionRecordWithRelations] = {
    SQL("""
    SELECT er.*, se.*, t.*
    FROM emotion_records er
    LEFT JOIN emotion_records_sub_emotions ers ON er.id = ers.emotion_record_id
    LEFT JOIN sub_emotions se ON ers.sub_emotion_id = se.id
    LEFT JOIN emotion_record_triggers ert ON er.id = ert.emotion_record_id
    LEFT JOIN triggers t ON ert.trigger_id = t.id
    WHERE er.id = {id}
  """).on("id" -> id).as(parser.singleOpt)
  }


  def insert(emotionRecordWithRelations: EmotionRecordWithRelations)(implicit connection: Connection): Option[Long] = {
    val emotionRecordId = new EmotionRecordDao().insert(emotionRecordWithRelations.emotionRecord)
    emotionRecordId.foreach { id =>
      emotionRecordWithRelations.triggers.foreach { trigger =>
        SQL("""
          INSERT INTO emotion_record_triggers (emotion_record_id, trigger_id)
          VALUES ({emotionRecordId}, {triggerId})
        """).on("emotionRecordId" -> id, "triggerId" -> trigger.id).executeInsert()
      }
    }
    emotionRecordId
  }

  def update(emotionRecordWithRelations: EmotionRecordWithRelations)(implicit connection: Connection): Int = {
    val updateCount = new EmotionRecordDao().update(emotionRecordWithRelations.emotionRecord)
    if (updateCount > 0) {
      SQL("DELETE FROM emotion_record_triggers WHERE emotion_record_id = {emotionRecordId}")
        .on("emotionRecordId" -> emotionRecordWithRelations.emotionRecord.id)
        .executeUpdate()

      emotionRecordWithRelations.triggers.foreach { trigger =>
        SQL("""
          INSERT INTO emotion_record_triggers (emotion_record_id, trigger_id)
          VALUES ({emotionRecordId}, {triggerId})
        """).on("emotionRecordId" -> emotionRecordWithRelations.emotionRecord.id, "triggerId" -> trigger.id)
          .executeInsert()
      }
    }
    updateCount
  }

  def delete(id: Int)(implicit connection: Connection): Int = {
    SQL("DELETE FROM emotion_record_triggers WHERE emotion_record_id = {id}")
      .on("id" -> id)
      .executeUpdate()

    new EmotionRecordDao().delete(id)
  }

   def findAllByUserId(userId: Int)(implicit connection: Connection): List[EmotionRecordWithRelations] = {
     SQL("""
       SELECT er.*, se.*, t.*
       FROM emotion_records er
       LEFT JOIN sub_emotions se ON er.sub_emotion_id = se.id
       LEFT JOIN emotion_record_triggers ert ON er.id = ert.emotion_record_id
       LEFT JOIN triggers t ON ert.trigger_id = t.id
       WHERE er.user_id = {userId}
     """).on("userId" -> userId).as(parser.*)
   }
}
