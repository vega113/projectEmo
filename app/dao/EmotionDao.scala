package dao

import anorm._
import dao.model.Emotion

import java.sql.Connection

class EmotionDao {

  def findAll()(implicit connection: Connection): List[Emotion] = {
    SQL("SELECT * FROM emotions").as(Emotion.parser.*)
  }

  def insert(emotion: Emotion)(implicit connection: Connection): Option[Long] = {
    SQL(
      """
        INSERT INTO emotions (id, emotion_name, emotion_type)
        VALUES ({id}, {emotionName}, {emotionType})
      """
    ).on(
      "id" -> emotion.id,
      "emotionName" -> emotion.emotionName,
      "emotionType" -> emotion.emotionType,
    ).executeInsert()
  }

  def update(emotion: Emotion)(implicit connection: Connection): Int = {
    SQL(
      """
        UPDATE emotions SET
          emotion_name = {emotionName},
          emotion_type = {emotionType}
        WHERE id = {id}
      """
    ).on(
      "id" -> emotion.id,
      "emotionName" -> emotion.emotionName,
      "emotionType" -> emotion.emotionType,
    ).executeUpdate()
  }

  def delete(id: String)(implicit connection: Connection): Int = {
    SQL("DELETE FROM emotions WHERE id = {id}").on(Symbol("id") -> id).executeUpdate()
  }

  def findById(id: String)(implicit connection: Connection): Option[Emotion] = {
    SQL("SELECT * FROM emotions WHERE id = {id}").on(Symbol("id") -> id).as(Emotion.parser.singleOpt)
  }
}
