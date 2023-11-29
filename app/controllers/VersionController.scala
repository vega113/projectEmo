package controllers

import buildinfo.BuildInfo
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import javax.inject.Inject

class VersionController  @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  def version:  Action[AnyContent] =
    Action {
      Ok(BuildInfo.toString)
    }
}
