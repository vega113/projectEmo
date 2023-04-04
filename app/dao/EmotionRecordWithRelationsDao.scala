package dao

import anorm._
import dao.model._
import java.sql.Connection

class EmotionRecordWithRelationsDao {
  import EmotionRecordWithRelations.parser

  def findAll()(implicit connection: Connection): List[EmotionRecordWithRelations] = {
    SQL("""
    SELECT er.id, er.user_id, er.emotion_id, er.intensity, se.id, se.sub_emotion_name, se.emotion_id, t.id, t.trigger_name, t.parent_id, t.description
    FROM emotion_records er
  """).as(parser.*)
  }



  def findById(id: Int)(implicit connection: Connection): Option[EmotionRecordWithRelations] = {
    SQL("""
    SELECT er.id, er.user_id, er.emotion_id, er.intensity, se.id, se.sub_emotion_name, se.emotion_id, t.id, t.trigger_name, t.parent_id, t.description
    FROM emotion_records er
    LEFT JOIN emotion_record_sub_emotions ers ON er.id = ers.emotion_record_id
    LEFT JOIN sub_emotions se ON ers.sub_emotion_id = se.id
    LEFT JOIN emotion_record_triggers ert ON er.id = ert.emotion_record_id
    LEFT JOIN triggers t ON ert.trigger_id = t.id
    WHERE er.id = {id}
  """).on("id" -> id).as(parser.singleOpt)
  }


  def insert(emotionRecordWithRelations: EmotionRecordWithRelations)(implicit connection: Connection): Option[Long] = {
    SQL(
      """
        INSERT INTO emotion_records (user_id, emotion_id, intensity)
        VALUES ({userId}, {emotionId}, {intensity})
      """
    ).on(
      Symbol("userId") -> emotionRecordWithRelations.emotionRecord.userId,
      Symbol("emotionId") -> emotionRecordWithRelations.emotionRecord.emotionId,
      Symbol("intensity") -> emotionRecordWithRelations.emotionRecord.intensity,
    ).executeInsert()
  }
  def update(emotionRecordWithRelations: EmotionRecordWithRelations)(implicit connection: Connection): Int = ???
  def delete(id: Int)(implicit connection: Connection): Int = {
    ???
  }

   def findAllByUserId(userId: Int)(implicit connection: Connection): List[EmotionRecordWithRelations] = {
     ???
   }
}
