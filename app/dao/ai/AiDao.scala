package dao.ai

import anorm.SQL
import dao.model.AiDbObj
import service.model

import java.sql.Connection
import scala.concurrent.Future

class AiDao {
  def insertAiAssistant(aiAssistant: model.AiAssistant)(implicit connection: Connection): Option[Long] = {
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
