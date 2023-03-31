package model

import java.time.{LocalDate, LocalDateTime}

object dao {

  case class User(id: Int, username: String, email: String, password: String, created: LocalDateTime)

  case class Emotion(id: String, emotionName: String, emotionType: String)

  case class SubEmotion(id: Int, subEmotionName: String, emotionId: String)

  case class EmotionRecord(id: Int, emotionId: String, subEmotionId: Int, intensity: Int, date: LocalDateTime)

  case class Trigger(id: Int, userId: Option[Int], description: Option[String])

  case class Note(id: Int, title: String, content: String, userId: Option[Int])

  case class Tag(id: Int, userId: Option[Int], tagName: String)

  case class EmotionRecordTag(emotionRecordId: Int, tagId: Int)

  case class NoteTag(noteId: Int, tagId: Int)



}
