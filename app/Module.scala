import auth.{JwtService, JwtServiceImpl}
import com.google.inject.AbstractModule
import dao.{DatabaseExecutionContext, DatabaseExecutionContextImpl}
import liquibase.LiquibaseRunner
import play.api.libs.concurrent.AkkaGuiceSupport

class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bind(classOf[LiquibaseRunner]).asEagerSingleton()
    bind(classOf[DatabaseExecutionContext]).to(classOf[DatabaseExecutionContextImpl]).asEagerSingleton()
    bind(classOf[JwtService]).to(classOf[JwtServiceImpl]).asEagerSingleton()
    bind(classOf[JwtService]).to(classOf[JwtServiceImpl]).asEagerSingleton()
  }
}
