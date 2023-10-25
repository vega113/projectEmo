package dao

import anorm._
import dao.model._

import java.sql.Connection
import javax.inject.Inject

class EmotionRecordDao @Inject()(emotionRecordSubEmotionDao: EmotionRecordSubEmotionDao,
                                 emotionRecordTriggerDao: EmotionRecordTriggerDao,
                                 emotionDao: EmotionDao,
                                 noteDao: NoteDao,
                                 tagDao: TagDao
                                ) {
  def findEmotionRecordIdByUserIdNoteId(userId: Long, noteId: Long)(implicit connection: Connection): Option[Long] = {
    SQL("SELECT emotion_record_id FROM emotion_record_notes inner join emotion_records on id=emotion_record_id" +
      " WHERE note_id = {noteId} and user_id = {userId}").
      on("noteId" -> noteId, "userId" -> userId).
      as(SqlParser.scalar[Long].singleOpt)
  }

  def findEmotionRecordIdByUserIdTagId(userId: Long, tagId: Long)(implicit connection: Connection): Option[Long] = {
    SQL("SELECT emotion_record_id FROM emotion_record_tags inner join emotion_records on id=emotion_record_id" +
      " WHERE tag_id = {tagId} and user_id = {userId}").
      on("tagId" -> tagId, "userId" -> userId).
      as(SqlParser.scalar[Long].singleOpt)
  }

  def findAllByUserIdAndDateRange(userId: Long, startDate: String, endDate: String)(implicit connection: Connection): List[EmotionRecord] = {
    val emotionRecords = SQL("SELECT * FROM emotion_records WHERE user_id = {userId} AND created BETWEEN {startDate} AND {endDate}").
      on("userId" -> userId, "startDate" -> startDate, "endDate" -> endDate).
      as(EmotionRecord.parser.*)
    populateListsByEmotionRecord(emotionRecords)
  }

  def findAll()(implicit connection: Connection): List[EmotionRecord] = {
    val emotionRecords = SQL("SELECT * FROM emotion_records").as(EmotionRecord.parser.*)
    populateListsByEmotionRecord(emotionRecords)
  }

  private def populateListsByEmotionRecord(emotionRecords: List[EmotionRecord])(implicit connection: Connection): List[EmotionRecord] = {
    for {
      emotionRecord <- emotionRecords
      id <- emotionRecord.id.toList
    } yield emotionRecord.copy(
      emotion = emotionRecord.emotion.map(emotion => emotion.id.flatMap(emotionId => emotionDao.findById(emotionId))) .
        getOrElse(emotionRecord.emotion) ,
      subEmotions = emotionRecordSubEmotionDao.findAllSubEmotionsByEmotionRecordId(id),
      triggers = emotionRecordTriggerDao.findAllTriggersByEmotionRecordId(id),
      notes = noteDao.findAllNotDeletedByEmotionRecordId(id),
      tags = tagDao.findAllByEmotionRecordId(id)
    )
  }

  def findByIdForUser(recordId: Long, userId: Long)(implicit connection: Connection): Option[EmotionRecord] = {
    val emotionRecordOpt = SQL("SELECT * FROM emotion_records WHERE id = {recordId} and user_id = {userId}" ).
      on("recordId" -> recordId, "userId" -> userId).
      as(EmotionRecord.parser.singleOpt)
    populateListsByEmotionRecord(emotionRecordOpt.toList).headOption
  }

  def findAllByUserId(userId: Long)(implicit connection: Connection): List[EmotionRecord] = {
    val emotionRecords = SQL("SELECT * FROM emotion_records WHERE user_id = {userId}").on("userId" -> userId).
      as(EmotionRecord.parser.*)
    populateListsByEmotionRecord(emotionRecords)
  }

  def insert(emotionRecord: EmotionRecord)(implicit connection: Connection): Option[Long] = {
    val idOpt: Option[Long] = SQL(
      """
      INSERT INTO emotion_records (emotion_type, emotion_id, user_id, intensity, created)
      VALUES ({emotionType}, {emotionId}, {userId}, {intensity}, {created})
    """).on("userId" -> emotionRecord.userId.getOrElse(throw new RuntimeException("User id is required.")),
      "emotionType" -> emotionRecord.emotionType,
      "emotionId" -> emotionRecord.emotion.flatMap(_.id),
      "intensity" -> emotionRecord.intensity,
      "created" -> emotionRecord.created)
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
    for {
      note <- emotionRecord.notes
    } yield {
      noteDao.insert(id, note)
    }
    tagDao.insert(id, emotionRecord.tags)
  }

  def update(emotionRecord: EmotionRecord)(implicit connection: Connection): Int = {
    val insertedCount = SQL(
      """
      UPDATE emotion_records
      SET emotion_id = {emotionId}, intensity = {intensity}
      WHERE id = {id}
    """).on("id" -> emotionRecord.id,
      "userId" -> emotionRecord.userId,
      "emotionId" -> emotionRecord.emotion.flatMap(_.id),
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
