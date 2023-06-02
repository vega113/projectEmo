package liquibase

import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.FileSystemResourceAccessor
import org.slf4j.{Logger, LoggerFactory}
import play.api.{Configuration, Environment}

import javax.inject.Inject

class LiquibaseRunner @Inject()(env: Environment, config: Configuration) {

  private val logger: Logger = LoggerFactory.getLogger(classOf[LiquibaseRunner])

  runMigrations()

  private def runMigrations(): Unit = {
    val dbConf = config.get[Configuration]("db.default")
    val liquibaseConf = config.get[Configuration]("liquibase")

    val url = dbConf.get[String]("url")
    val username = dbConf.get[String]("username")
    val password = dbConf.get[String]("password")
    logger.info(s"Running Liquibase migrations on $url")

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
}
