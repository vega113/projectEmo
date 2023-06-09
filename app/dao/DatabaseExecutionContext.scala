package dao

import java.sql.Connection
import javax.inject.Inject

import akka.actor.ActorSystem
import play.api.db.Database
import play.api.libs.concurrent.CustomExecutionContext

trait DatabaseExecutionContext {
  def withConnection[A](block: Connection => A): A
}

class DatabaseExecutionContextImpl @Inject()(db: Database, actorSystem: ActorSystem)
  extends CustomExecutionContext(actorSystem, "database.dispatcher") with DatabaseExecutionContext {

  def withConnection[A](block: Connection => A): A = {
    val connection = db.getConnection()
    try {
      val out = block(connection)
      out
    } finally {
      connection.close()
    }
  }
}

