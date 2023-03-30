package liquibase

import com.google.inject.{AbstractModule, Provides}
import play.api.inject.ApplicationLifecycle
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.{Configuration, Environment}

import javax.inject.Singleton

class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bind(classOf[LiquibaseRunner]).asEagerSingleton()
  }
}

