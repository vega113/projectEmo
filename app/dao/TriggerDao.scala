package dao

import anorm._
import dao.model._

import java.sql.Connection

class TriggerDao {
  def findAll()(implicit connection: Connection): List[Trigger] = {
    SQL("SELECT * FROM triggers").as(Trigger.parser.*)
  }

  def findById(id: Int)(implicit connection: Connection): Option[Trigger] = {
    SQL("SELECT * FROM triggers WHERE id = {id}").on("id" -> id).as(Trigger.parser.singleOpt)
  }

  def insert(trigger: Trigger)(implicit connection: Connection): Option[Long] = {
    SQL("INSERT INTO triggers (user_id, description) VALUES ({userId}, {description})")
      .on("userId" -> trigger.userId, "description" -> trigger.description)
      .executeInsert()
  }

  def update(trigger: Trigger)(implicit connection: Connection): Int = {
    SQL("UPDATE triggers SET user_id = {userId}, description = {description} WHERE id = {id}")
      .on("id" -> trigger.id, "userId" -> trigger.userId, "description" -> trigger.description)
      .executeUpdate()
  }

  def delete(id: Int)(implicit connection: Connection): Int = {
    SQL("DELETE FROM triggers WHERE id = {id}").on("id" -> id).executeUpdate()
  }
}
