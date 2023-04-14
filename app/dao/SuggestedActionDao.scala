package dao

import anorm.{Macro, SQL}
import dao.model.SuggestedAction

import java.sql.Connection

class SuggestedActionDao {
  def findAll()(implicit connection: Connection): List[SuggestedAction] = {
    SQL("SELECT * FROM suggested_actions").as(SuggestedAction.parser.*)
  }

  def findAllBySubEmotionId(subEmotionId: String)(implicit connection: Connection): List[SuggestedAction] = {
    SQL("SELECT * FROM suggested_actions WHERE suggested_action_id IN (SELECT parent_suggested_action_id FROM sub_emotion_suggested_actions WHERE parent_sub_emotion_id = {subEmotionId})")
      .on("subEmotionId" -> subEmotionId)
      .as(SuggestedAction.parser.*)
  }
}
