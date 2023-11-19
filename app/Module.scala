import com.google.inject.AbstractModule
import liquibase.LiquibaseRunner
import play.api.libs.concurrent.AkkaGuiceSupport

class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bind(classOf[LiquibaseRunner]).asEagerSingleton()
    bind(classOf[ShutdownHook]).asEagerSingleton()
  }
}
