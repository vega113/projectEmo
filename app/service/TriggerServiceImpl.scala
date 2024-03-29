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
  def deleteByEmotionRecordId(id: Long, userId: Long) : Future[Boolean]

  def findAll(): Future[List[Trigger]]

  def insert(emotionRecordId: Long, triggers: List[Trigger]): Future[Boolean]

  def linkTriggerToEmotionRecord(triggerId: Long, emotionRecordId: Long): Future[Boolean]

  def findByName(triggerName: String): Future[Trigger]
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

  override def insert(triggerParentId: Long, triggers: List[Trigger]): Future[Boolean] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      val triggerIds = triggers.map(trigger => {
        triggerDao.insert(trigger, triggerParentId) match {
          case Some(id) =>
            logger.info(s"Inserted trigger ${trigger.triggerName} with id $id")
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

    override def deleteByEmotionRecordId(id: Long, userId: Long): Future[Boolean] = {
      databaseExecutionContext.withConnection({ implicit connection =>
        triggerDao.deleteByEmotionRecordId(id, userId)
        Future.successful(true)
      })
    }

  override def findByName(triggerName: String): Future[Trigger] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      Future.successful(triggerDao.findByName(triggerName))
    })
  }
}
