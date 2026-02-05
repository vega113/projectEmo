package service.ai

import ch.qos.logback.classic.{Level, Logger}
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import dao.DatabaseExecutionContext
import dao.ai.AiDao
import dao.model.AiDbObj
import org.mockito.ArgumentMatchers.{any => anyArg}
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.slf4j.LoggerFactory
import play.api.libs.json.Json

import java.sql.Connection
import scala.concurrent.Await
import scala.concurrent.duration._

class AiDbServiceSpec extends AnyWordSpec with Matchers with MockitoSugar {
  "AiDbServiceImpl.saveAiResponse" should {
    "log error with exception when insert throws" in {
      val aiDao = mock[AiDao]
      when(aiDao.insert(anyArg[AiDbObj])(anyArg[Connection]))
        .thenThrow(new RuntimeException("boom"))

      val databaseExecutionContext = new DatabaseExecutionContext {
        override def withConnection[A](block: Connection => A): A = block(null)
      }

      val service = new AiDbServiceImpl(databaseExecutionContext, aiDao)

      val logger = LoggerFactory.getLogger(classOf[AiDbServiceImpl]).asInstanceOf[Logger]
      val appender = new ListAppender[ILoggingEvent]()
      appender.start()
      logger.addAppender(appender)

      try {
        val result = Await.result(
          service.saveAiResponse(1L, Json.obj("ok" -> true)),
          2.seconds
        )

        result shouldBe None

        val errorEvents = appender.list.toArray(new Array[ILoggingEvent](appender.list.size()))
          .toList
          .filter(_.getLevel == Level.ERROR)

        errorEvents.exists(event => Option(event.getThrowableProxy).isDefined) shouldBe true
      } finally {
        logger.detachAppender(appender)
        appender.stop()
      }
    }
  }
}
