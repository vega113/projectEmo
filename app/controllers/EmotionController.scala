package controllers

import dao._
import dao.model.Emotion
import play.api.libs.json._
import play.api.mvc._

import javax.inject._
import scala.concurrent.Future

@Singleton
class EmotionController @Inject()(cc: ControllerComponents, emotionDao: EmotionDao, dbExecutionContext: DatabaseExecutionContext)
  extends AbstractController(cc) {

  def getAllEmotions: Action[AnyContent] = Action.async { implicit request =>
    dbExecutionContext.withConnection { implicit connection =>
      val emotions = emotionDao.findAll()
      Future.successful(Ok(Json.toJson(emotions)))
    }
  }

  def getEmotionById(id: String): Action[AnyContent] = Action.async { implicit request =>
    dbExecutionContext.withConnection { implicit connection =>
      val emotion = emotionDao.findById(id)
      Future.successful(emotion.map(e => Ok(Json.toJson(e))).getOrElse(NotFound))
    }
  }

  def insertEmotion: Action[JsValue] = Action(parse.json).async { implicit request =>
    dbExecutionContext.withConnection { implicit connection =>
      request.body.validate[Emotion].fold(
        errors => {
          Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
        },
        emotion => {
          emotionDao.insert(emotion)
          Future.successful(Created(Json.toJson(emotion)))
        }
      )
    }
  }

  def updateEmotion(id: String): Action[JsValue] = Action(parse.json).async { implicit request =>
    dbExecutionContext.withConnection { implicit connection =>
      request.body.validate[Emotion].fold(
        errors => {
          Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
        },
        emotion => {
          val affectedRows = emotionDao.update(emotion.copy(id = id))
          if (affectedRows > 0) {
            Future.successful(Ok(Json.toJson(emotion)))
          } else {
            Future.successful(NotFound)
          }
        }
      )
    }
  }

  def deleteEmotion(id: String): Action[AnyContent] = Action.async { implicit request =>
    dbExecutionContext.withConnection { implicit connection =>
      val affectedRows = emotionDao.delete(id)
      if (affectedRows > 0) {
        Future.successful(Ok(Json.obj("message" -> s"Emotion with id $id has been deleted.")))
      } else {
        Future.successful(NotFound)
      }
    }
  }
}
