package service

import com.google.inject.ImplementedBy
import dao.model.Trigger
import dao.{DatabaseExecutionContext, TriggerDao}

import javax.inject.Inject
import scala.concurrent.Future

@ImplementedBy(classOf[TriggerServiceImpl])
trait TriggerService {
  def findAll(): Future[List[Trigger]]
  def insert(emotionRecordId: Long, triggers: List[Trigger]): Future[Boolean]
}
class TriggerServiceImpl @Inject()(
                                    triggerDao: TriggerDao,
                                   databaseExecutionContext: DatabaseExecutionContext) extends TriggerService {
  def findAll(): Future[List[Trigger]] = {
    Future.successful(databaseExecutionContext.withConnection { implicit connection =>
      triggerDao.findAll()
    })
  }

  override def insert(emotionRecordId: Long, triggers: List[Trigger]): Future[Boolean] = {
    databaseExecutionContext.withConnection({ implicit connection =>
      val triggerIds = triggers.map(trigger => {
        triggerDao.insert(trigger, emotionRecordId)
      })
      Future.successful(triggerIds.length == triggers.size)
    })
  }
}
