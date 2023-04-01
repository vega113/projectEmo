package dao

import java.sql.Connection
import javax.inject.Inject

import akka.actor.ActorSystem
import play.api.db.Database
import play.api.libs.concurrent.CustomExecutionContext

class DatabaseExecutionContext @Inject()(db: Database, actorSystem: ActorSystem)
  extends CustomExecutionContext(actorSystem, "database.dispatcher") {

  def withConnection[A](block: Connection => A): A = {
    val connection = db.getConnection()
    try {
      block(connection)
    } finally {
      connection.close()
    }
  }
}

