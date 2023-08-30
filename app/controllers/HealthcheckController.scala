package controllers

import play.api.mvc.{AbstractController, ControllerComponents}

import javax.inject.Inject

class HealthcheckController @Inject()(val cc: ControllerComponents) extends AbstractController(cc){
  def healthcheck() = Action {
    Ok("healthy")
  }
}
