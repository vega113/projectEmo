package service

import akka.event.slf4j
import com.google.inject.ImplementedBy
import dao.model.Trigger
import dao.{DatabaseExecutionContext, TriggerDao}
import org.slf4j.Logger

import javax.inject.Inject
import scala.concurrent.Future

@ImplementedBy(classOf[TriggerServiceImpl])
trait TriggerService {
  def findAll(): Future[List[Trigger]]

  def insert(emotionRecordId: Long, triggers: List[Trigger]): Future[Boolean]

  def linkTriggerToEmotionRecord(triggerId: Long, emotionRecordId: Long): Future[Boolean]
}
class TriggerServiceImpl @Inject()(
                                    triggerDao: TriggerDao,
                                    databaseExecutionContext: DatabaseExecutionContext) extends TriggerService {
  val logger: Logger = slf4j.Logger("TriggerServiceImpl")

  def findAll(): Future[List[Trigger]] = {
    Future.successful(databaseExecutionContext.withConnection { implicit connection =>
      triggerDao.findAll()
    })
  }

  override def insert(emotionRecordId: Long, triggers: List[Trigger]): Future[Boolean] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      val triggerIds = triggers.map(trigger => {
        triggerDao.insert(trigger, emotionRecordId) match {
          case Some(id) =>
            triggerDao.linkTriggerToEmotionRecord(id, emotionRecordId)
            id
          case None =>
            logger.error(s"Failed to insert trigger ${trigger.triggerName}")
        }
      })
      Future.successful(triggerIds.length == triggers.size)
    })
  }

  override def linkTriggerToEmotionRecord(triggerId: Long, emotionRecordId: Long): Future[Boolean] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      triggerDao.linkTriggerToEmotionRecord(triggerId, emotionRecordId)
      Future.successful(true)
    })
  }
}
