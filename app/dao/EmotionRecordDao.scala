package dao

import anorm._
import dao.model._
import service.DateTimeService

import java.sql.Connection
import javax.inject.Inject

class EmotionRecordDao @Inject()(emotionRecordSubEmotionDao: EmotionRecordSubEmotionDao,
                                 emotionRecordTriggerDao: EmotionRecordTriggerDao,
                                 emotionDao: EmotionDao,
                                 noteDao: NoteDao,
                                 tagDao: TagDao,
                                 dateTimeService: DateTimeService
                                ) {
  def linkSubEmotionToEmotionRecord(subEmotionId: String, emotionRecordId: Long)(implicit connection: Connection): Option[Long] = {
    SQL(
      """
        |UPDATE emotion_records
        |SET sub_emotion_id = {subEmotionId}
        |WHERE id = {emotionRecordId}
        |""".stripMargin).on("subEmotionId" -> subEmotionId, "emotionRecordId" -> emotionRecordId).executeInsert()
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
      emotion = emotionRecord.emotion.map(emotion => emotion.id.flatMap(emotionId => emotionDao.findById(emotionId))).
        getOrElse(emotionRecord.emotion),
      subEmotions = emotionRecordSubEmotionDao.findAllSubEmotionsByEmotionRecordId(id),
      triggers = emotionRecordTriggerDao.findAllTriggersByEmotionRecordId(id),
      notes = noteDao.findAllNotDeletedByEmotionRecordId(id),
      tags = tagDao.findAllByEmotionRecordId(id)
    )
  }

  def findByIdForUser(recordId: Long, userId: Long)(implicit connection: Connection): Option[EmotionRecord] = {
    val emotionRecordOpt = SQL(
      """
     SELECT * FROM emotion_records WHERE id = {recordId} and user_id = {userId}
      """).
      on("recordId" -> recordId, "userId" -> userId).
      as(EmotionRecord.parser.singleOpt)
    populateListsByEmotionRecord(emotionRecordOpt.toList).headOption
  }

  def findAllByUserId(userId: Long)(implicit connection: Connection): List[EmotionRecord] = {
    val emotionRecords = SQL("SELECT * FROM emotion_records WHERE user_id = {userId} and is_deleted <> true").
      on("userId" -> userId).
      as(EmotionRecord.parser.*)
    populateListsByEmotionRecord(emotionRecords)
  }

  def insert(emotionRecord: EmotionRecord)(implicit connection: Connection): Option[Long] = {
    val idOpt: Option[Long] = SQL(
      """
    INSERT INTO emotion_records (emotion_type, emotion_id, user_id, intensity, created, is_deleted, sub_emotion_id, trigger_id)
    VALUES ({emotionType}, {emotionId}, {userId}, {intensity}, {created}, {isDeleted}, {subEmotionId}, {triggerId})
    """).on(
      "userId" -> emotionRecord.userId.getOrElse(throw new RuntimeException("User id is required.")),
      "emotionType" -> emotionRecord.emotionType,
      "emotionId" -> emotionRecord.emotion.flatMap(_.id),
      "intensity" -> emotionRecord.intensity,
      "created" -> emotionRecord.created,
      "isDeleted" -> emotionRecord.isDeleted.getOrElse(false), // Assuming isDeleted is an Option[Boolean]
      "subEmotionId" -> emotionRecord.subEmotionId,
      "triggerId" -> emotionRecord.triggerId
    ).executeInsert()
    idOpt
  }


  def update(emotionRecord: EmotionRecord)(implicit connection: Connection): Int = {
    val updatedCount = SQL(
      """
    UPDATE emotion_records
    SET emotion_type = {emotionType}, emotion_id = {emotionId}, user_id = {userId}, intensity = {intensity},
    is_deleted = {isDeleted}, last_updated = {lastUpdated}
    WHERE id = {id}
  """).on("id" -> emotionRecord.id.getOrElse(throw new RuntimeException("Id is required.")),
        "userId" -> emotionRecord.userId.getOrElse(throw new RuntimeException("User id is required.")),
        "emotionType" -> emotionRecord.emotionType,
        "emotionId" -> emotionRecord.emotion.flatMap(_.id),
        "intensity" -> emotionRecord.intensity,
        "isDeleted" -> emotionRecord.isDeleted,
        "lastUpdated" -> dateTimeService.now(),
        "created" -> emotionRecord.created)
      .executeUpdate()
    updatedCount
  }

  def deleteByIdAndUserId(id: Long, userId: Long)(implicit connection: Connection): Boolean = {
    val deleted = SQL("DELETE FROM emotion_records WHERE id = {id} AND user_id = {userId}").
      on("id" -> id, "userId" -> userId).
      executeUpdate()
    deleted > 0
  }
}
