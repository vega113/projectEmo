import org.slf4j.Logger

import scala.concurrent.Future
import play.api.inject.ApplicationLifecycle

import javax.inject._

class ShutdownHook @Inject()(lifecycle: ApplicationLifecycle) {
  val logger: Logger = org.slf4j.LoggerFactory.getLogger("ShutdownHook")

  lifecycle.addStopHook { () =>
    logger.info("ShutdownHook is running")
    val threadMXBean = java.lang.management.ManagementFactory.getThreadMXBean
    val threadInfos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds, 100)
    val stackTrace = threadInfos.map(threadInfo => {
      s"""
      '${threadInfo.getThreadName}': ${threadInfo.getThreadState}
      at ${threadInfo.getStackTrace.mkString("\n  at ")}
      """
    }).mkString("\n")
    logger.info(s"Thread dump:\n$stackTrace")
    Future.successful(())
  }
}