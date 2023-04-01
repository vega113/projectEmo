package dao

import anorm._
import dao.model._

import java.sql.Connection

class SubEmotionDao {
  def findAll()(implicit connection: Connection): List[SubEmotion] = {
    SQL("SELECT * FROM sub_emotions").as(SubEmotion.parser.*)
  }

  def findById(id: Int)(implicit connection: Connection): Option[SubEmotion] = {
    SQL("SELECT * FROM sub_emotions WHERE id = {id}").on("id" -> id).as(SubEmotion.parser.singleOpt)
  }

  def insert(subEmotion: SubEmotion)(implicit connection: Connection): Option[Long] = {
    SQL("INSERT INTO sub_emotions (sub_emotion_name, emotion_id) VALUES ({subEmotionName}, {emotionId})")
      .on("subEmotionName" -> subEmotion.subEmotionName, "emotionId" -> subEmotion.emotionId)
      .executeInsert()
  }

  def update(subEmotion: SubEmotion)(implicit connection: Connection): Int = {
    SQL("UPDATE sub_emotions SET sub_emotion_name = {subEmotionName}, emotion_id = {emotionId} WHERE id = {id}")
      .on("id" -> subEmotion.id, "subEmotionName" -> subEmotion.subEmotionName, "emotionId" -> subEmotion.emotionId)
      .executeUpdate()
  }

  def delete(id: Int)(implicit connection: Connection): Int = {
    SQL("DELETE FROM sub_emotions WHERE id = {id}").on("id" -> id).executeUpdate()
  }
}
