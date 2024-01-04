package service.ai

import com.google.inject.ImplementedBy
import dao.model.EmotionDetectionResult
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import service.model._
import service.serviceModel.ChatGptApiResponse

import javax.inject.{Inject, Named}
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

@ImplementedBy(classOf[CompositeEmotionDetectionServiceImpl])
trait EmotionDetectionService {
  def detectEmotion(request: DetectEmotionRequest): Future[EmotionDetectionResult]
}

class CompositeEmotionDetectionServiceImpl @Inject()(@Named("ChatGpt") v1: EmotionDetectionService,
                                                     @Named("ChatGptAssistant") v2: EmotionDetectionService
                                                    )(implicit ec: ExecutionContext) extends EmotionDetectionService {
  override def detectEmotion(request: DetectEmotionRequest): Future[EmotionDetectionResult] = {
    val v1EmotionFuture = v1.detectEmotion(request)
    val v2EmotionFuture = v2.detectEmotion(request)

    Future.firstCompletedOf(Seq(v1EmotionFuture, v2EmotionFuture)).map { resp =>
      resp
    }
  }
}
