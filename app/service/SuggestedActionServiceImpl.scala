package service

import com.google.inject.ImplementedBy
import dao.model.SuggestedAction
import dao.{DatabaseExecutionContext, SuggestedActionDao}

import javax.inject.Inject
import scala.concurrent.Future

@ImplementedBy(classOf[SuggestedActionServiceImpl])
trait SuggestedActionService {
  def findAllBySubEmotionId(id: String): Future[List[SuggestedAction]]
}
class SuggestedActionServiceImpl @Inject()(
                                    suggestedActionDao: SuggestedActionDao,
                                   databaseExecutionContext: DatabaseExecutionContext) extends SuggestedActionService {
  def findAllBySubEmotionId(id: String): Future[List[SuggestedAction]] = {
    Future.successful(databaseExecutionContext.withConnection { implicit connection =>
      suggestedActionDao.findAllBySubEmotionId(id)
    })
  }
}
