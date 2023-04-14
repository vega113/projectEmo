package controllers

import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import service.SuggestedActionService

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global

class SuggestedActionController @Inject()(cc: ControllerComponents,
                                          suggestedActionService: SuggestedActionService)
  extends AbstractController(cc) {

    def findAllBySubEmotionId(subEmotionId: String): Action[AnyContent] = Action async {
      suggestedActionService.findAllBySubEmotionId(subEmotionId).map(suggestedActions => Ok(Json.toJson(suggestedActions)))
    }

  }
