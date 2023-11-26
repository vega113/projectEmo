import play.api
import play.api.Configuration

import scala.concurrent.Future
import play.api.inject.ApplicationLifecycle

import javax.inject._

class ShutdownHook @Inject()(lifecycle: ApplicationLifecycle, config: Configuration) {
  private lazy val logger = play.api.Logger(getClass)

  private def threadDump(): Unit = {
    val threadMXBean = java.lang.management.ManagementFactory.getThreadMXBean
    val threadInfos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds, 100)
    val stackTrace = threadInfos.map(threadInfo => {
      s"""
      '${threadInfo.getThreadName}': ${threadInfo.getThreadState}
      at ${threadInfo.getStackTrace.mkString("\n  at ")}
      """
    }).mkString("\n")
    logger.info(s"Thread dump:\n$stackTrace")
  }

  lifecycle.addStopHook { () =>
    logger.info("ShutdownHook is running")
    if (config.get[Boolean]("emo.threadDumpOnShutdown")) {
      logger.info("Thread dump on shutdown is enabled")
      threadDump()
    } else {
      logger.info("Thread dump on shutdown is disabled")
    }
    Future.successful(())
  }
}