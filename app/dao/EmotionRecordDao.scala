package dao

import anorm._
import dao.model._

import java.sql.Connection

class EmotionRecordDao {
  def findAll()(implicit connection: Connection): List[EmotionRecord] = {
    SQL("SELECT * FROM emotion_records").as(EmotionRecord.parser.*)
  }




  def findById(id: Int)(implicit connection: Connection): Option[EmotionRecord] = {
    SQL("SELECT * FROM emotion_records WHERE id = {id}").on("id" -> id).as(EmotionRecord.parser.singleOpt)
  }

  def insert(emotionRecord: EmotionRecord)(implicit connection: Connection): Option[Long] = {
    SQL("""
      INSERT INTO emotion_records (user_id, emotion_id, intensity)
      VALUES ({emotionId}, {intensity})
    """).on("userId" -> emotionRecord.userId, "emotionId" -> emotionRecord.emotionId,
      "intensity" -> emotionRecord.intensity)
      .executeInsert()
  }

  def update(emotionRecord: EmotionRecord)(implicit connection: Connection): Int = {
    SQL("""
      UPDATE emotion_records
      SET emotion_id = {emotionId}, intensity = {intensity}
      WHERE id = {id}
    """).on("id" -> emotionRecord.id,
      "userId" -> emotionRecord.userId,
      "emotionId" -> emotionRecord.emotionId,
      "intensity" -> emotionRecord.intensity)
      .executeUpdate()
  }

  def delete(id: Int)(implicit connection: Connection): Int = {
    SQL("DELETE FROM emotion_records WHERE id = {id}").on("id" -> id).executeUpdate()
  }
}
