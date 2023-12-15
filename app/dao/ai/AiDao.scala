package dao.ai

import anorm.SQL
import dao.AiAssistant
import dao.model.AiDbObj
import service.model.AiThread

import java.sql.Connection

class AiDao {
  def insertAiThread(aiThread: AiThread)(implicit connection: Connection): Option[Long] = {
    SQL(
      """
        |INSERT INTO ai_threads (external_id, user_id, thread_type, is_deleted, created)
        |VALUES ({externalId}, {userId}, {threadType}, false)
        |""".stripMargin
    ).on(
      "externalId" -> aiThread.externalId,
      "userId" -> aiThread.userId,
      "threadType" -> aiThread.threadType
    ).executeInsert()
  }

  def fetchThreadById(id: Long)(implicit connection: Connection): Option[AiThread] = {
    SQL(
      """
        |SELECT id, external_id, user_id, thread_type, is_deleted, created
        |FROM ai_threads
        |WHERE id = {id}
        |""".stripMargin
    ).on(
      "id" -> id
    ).as(AiThread.parser.singleOpt)
  }

  def fetchThreadByUserIdAndType(userId: Long, assistantType: String)(implicit connection: Connection): Option[AiThread] = {
    SQL(
      """
        |SELECT id, external_id, user_id, thread_type, is_deleted, created
        |FROM ai_threads
        |WHERE user_id = {userId}
        |AND thread_type = {threadType}
        |""".stripMargin
    ).on(
      "userId" -> userId,
      "threadType" -> assistantType
    ).as(AiThread.parser.singleOpt)
  }

  def fetchThreadByExternalId(externalId: String)(implicit connection: Connection): Option[AiThread] = {
    SQL(
      """
        |SELECT id, external_id, name, description, is_default, created, last_updated, created_at_provider, assistant_type
        |FROM ai_assistants
        |WHERE external_id = {externalId}
        |""".stripMargin
    ).on(
      "externalId" -> externalId
    ).as(AiThread.parser.singleOpt)
  }

  def fetchDefaultAiAssistantForType(assistantType: String)(implicit connection: Connection): Option[AiAssistant] = {
    SQL(
      """
        |SELECT id, external_id, name, description, is_default, created, last_updated, created_at_provider, assistant_type
        |FROM ai_assistants
        |WHERE assistant_type = {assistantType}
        |AND is_default = true
        |""".stripMargin
    ).on(
      "assistantType" -> assistantType
    ).as(AiAssistant.parser.singleOpt)
  }

  def fetchAiAssistantByExternalId(externalId: String)(implicit connection: Connection): Option[AiAssistant] = {
    SQL(
      """
        |SELECT id, external_id, name, description, is_default, created, last_updated, created_at_provider, assistant_type
        |FROM ai_assistants
        |WHERE external_id = {externalId}
        |""".stripMargin
    ).on(
      "externalId" -> externalId
    ).as(AiAssistant.parser.singleOpt)
  }

  def insertAiAssistant(aiAssistant: AiAssistant)(implicit connection: Connection): Option[Long] = {
    SQL(
      """
        |INSERT INTO ai_assistants (external_id, name, description, is_default, is_deleted, created, last_updated,
        |created_at_provider, assistant_type)
        |VALUES ({externalId}, {name}, {description}, {isDefault}, false, {created}, {lastUpdated}, {createdAtProvider},
        | {assistantType})
        |""".stripMargin
    ).on(
      "externalId" -> aiAssistant.externalId,
      "name" -> aiAssistant.name,
      "description" -> aiAssistant.description,
      "isDefault" -> aiAssistant.isDefault,
      "created" -> aiAssistant.created,
      "lastUpdated" -> aiAssistant.lastUpdated,
      "createdAtProvider" -> aiAssistant.createdAtProvider,
      "assistantType" -> aiAssistant.assistantType
    ).executeInsert()
  }

  def deleteAiAssistantByExternalId(externalId: String)(implicit connection: Connection): Boolean = {
    val count: Option[Long] = SQL(
      """
        |UPDATE ai_assistants
        |SET is_deleted = true
        |WHERE external_id = {externalId}
        |""".stripMargin
    ).on(
      "externalId" -> externalId
    ).executeInsert()
    count match {
      case Some(value) => value > 0
      case None => false
    }
  }

  def insert(response: AiDbObj)(implicit connection: Connection): Option[Long] = {
    SQL(
      """
        |INSERT INTO ai_responses (response, user_id)
        |VALUES ({response}, {userId})
        |""".stripMargin)
      .on(
        "response" -> response.response, "userId" -> response.userId
      ).executeInsert()
  }
}
