package service

import com.google.inject.ImplementedBy
import dao.model.Trigger
import dao.{DatabaseExecutionContext, TriggerDao}

import javax.inject.Inject
import scala.concurrent.Future

@ImplementedBy(classOf[TriggerServiceImpl])
trait TriggerService {
  def findAll(): Future[List[Trigger]]
}
class TriggerServiceImpl @Inject()(
                                    triggerDao: TriggerDao,
                                   databaseExecutionContext: DatabaseExecutionContext) extends TriggerService {
  def findAll(): Future[List[Trigger]] = {
    Future.successful(databaseExecutionContext.withConnection { implicit connection =>
      triggerDao.findAll()
    })
  }
}
