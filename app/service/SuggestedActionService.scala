package service

import com.google.inject.ImplementedBy
import dao.model.{EmotionRecord, SuggestedAction}
import dao.{DatabaseExecutionContext, EmotionRecordDao, SuggestedActionDao}

import javax.inject.Inject
import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

@ImplementedBy(classOf[SuggestedActionServiceImpl])
trait SuggestedActionService {

}
class SuggestedActionServiceImpl @Inject()(
                                            emotionDataService: EmotionDataService
                                          ) extends SuggestedActionService {

}
