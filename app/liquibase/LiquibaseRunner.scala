package liquibase

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Inject
import play.api.{Configuration, Environment}
import play.api.inject.ApplicationLifecycle
import liquibase.{Contexts, Liquibase}
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.FileSystemResourceAccessor
import org.slf4j.LoggerFactory

import scala.concurrent.Future

class LiquibaseRunner @Inject()(env: Environment, config: Configuration, lifecycle: ApplicationLifecycle) {

  private val logger: Logger = LoggerFactory.getLogger("LiquibaseRunner")

  runMigrations()

  def runMigrations(): Unit = {
    val dbConf = config.get[Configuration]("db.default")
    val liquibaseConf = config.get[Configuration]("liquibase")

    val url = dbConf.get[String]("url")
    val username = dbConf.get[String]("username")
    val password = dbConf.get[String]("password")
    val driver = dbConf.get[String]("driver")

    val changeLogFile = liquibaseConf.get[String]("changeLogFile")

    val connection = java.sql.DriverManager.getConnection(url, username, password)
    val jdbcConnection = new JdbcConnection(connection)

    try {
      val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection)
      val resourceAccessor = new  FileSystemResourceAccessor(new java.io.File(env.rootPath.getAbsolutePath))
      val liquibase = new Liquibase(changeLogFile, resourceAccessor, database)
      liquibase.update(new Contexts())
      logger.info("Liquibase migrations successfully applied.")
    } catch {
      case e: Exception =>
        logger.error("Error applying Liquibase migrations", e)
    } finally {
      if (connection != null) {
        connection.close()
      }
    }
  }

  lifecycle.addStopHook { () =>
    Future.successful(runMigrations())
  }
}
