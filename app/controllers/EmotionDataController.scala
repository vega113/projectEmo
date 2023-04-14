package controllers

import auth.AuthenticatedAction
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import service.EmotionDataService

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global

class EmotionDataController @Inject()(cc: ControllerComponents,
                                      emotionDataService: EmotionDataService)
  extends AbstractController(cc) {
  def fetchEmotionData(): Action[AnyContent] = Action  async {
    emotionDataService.fetchEmotionData().map(emotionData => Ok(Json.toJson(emotionData)))
  }
}
