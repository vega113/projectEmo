import org.slf4j.Logger
import play.api.Configuration

import scala.concurrent.Future
import play.api.inject.ApplicationLifecycle

import javax.inject._

class ShutdownHook @Inject()(lifecycle: ApplicationLifecycle, config: Configuration) {
  val logger: Logger = org.slf4j.LoggerFactory.getLogger("ShutdownHook")

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
      threadDump()
    }
    Future.successful(())
  }
}