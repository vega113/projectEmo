package dao

import java.sql.Connection
import javax.inject.Inject
import akka.actor.ActorSystem
import com.google.inject.ImplementedBy
import play.api.db.Database
import play.api.libs.concurrent.CustomExecutionContext

@ImplementedBy(classOf[DatabaseExecutionContextImpl])
trait DatabaseExecutionContext {
  def withConnection[A](block: Connection => A): A
}

class DatabaseExecutionContextImpl @Inject()(db: Database, actorSystem: ActorSystem)
  extends CustomExecutionContext(actorSystem, "database.dispatcher") with DatabaseExecutionContext {

  def withConnection[A](block: Connection => A): A = {
    db.withConnection { connection =>
      block(connection)
    }
  }
}

