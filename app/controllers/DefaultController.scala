package controllers

import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import javax.inject.Inject

class DefaultController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  def defaultOptions(all: String): Action[AnyContent] = Action {
    Ok.withHeaders(
      "Access-Control-Allow-Origin" -> "*",
      "Access-Control-Allow-Headers" -> "Origin, X-Requested-With, Content-Type, Accept, Authorization",
      "Access-Control-Allow-Methods" -> "GET, POST, PUT, DELETE, OPTIONS"
    )
  }
}
