package controllers

import dao._
import dao.model.EmotionRecord
import play.api.libs.json._
import play.api.mvc._

import javax.inject._
import scala.concurrent.Future

@Singleton
class EmotionRecordController @Inject()(cc: ControllerComponents, emotionRecordDao: EmotionRecordDao, dbExecutionContext: DatabaseExecutionContext)
  extends AbstractController(cc) {

  def findAll(): Action[AnyContent] = Action.async { implicit request =>
    dbExecutionContext.withConnection { implicit connection =>
      val emotions = emotionRecordDao.findAll()
      Future.successful(Ok(Json.toJson(emotions)))
    }
  }

  def findById(id: Int): Action[AnyContent] = Action.async { implicit request =>
    dbExecutionContext.withConnection { implicit connection =>
      val emotion = emotionRecordDao.findById(id)
      Future.successful(emotion.map(e => Ok(Json.toJson(e))).getOrElse(NotFound))
    }
  }

  def insert(): Action[JsValue] = Action(parse.json).async { implicit request =>
    dbExecutionContext.withConnection { implicit connection =>
      request.body.validate[EmotionRecord].fold(
        errors => {
          Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
        },
        emotionRecord => {
          emotionRecordDao.insert(emotionRecord)
          Future.successful(Created(Json.toJson(emotionRecord)))
        }
      )
    }
  }

  def update(id: Int): Action[JsValue] = Action(parse.json).async { implicit request =>
    dbExecutionContext.withConnection { implicit connection =>
      request.body.validate[EmotionRecord].fold(
        errors => {
          Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
        },
        emotionRecord => {
          val affectedRows = emotionRecordDao.update(emotionRecord.copy(id = Option(id)))
          if (affectedRows > 0) {
            Future.successful(Ok(Json.toJson(emotionRecord)))
          } else {
            Future.successful(NotFound)
          }
        }
      )
    }
  }

  def delete(id: Int): Action[AnyContent] = Action.async { implicit request =>
    dbExecutionContext.withConnection { implicit connection =>
      val affectedRows = emotionRecordDao.delete(id)
      if (affectedRows > 0) {
        Future.successful(Ok(Json.obj("message" -> s"Emotion with id $id has been deleted.")))
      } else {
        Future.successful(NotFound)
      }
    }
  }

  def findAllByUserId(userId: Int): Action[AnyContent] = Action.async { implicit request =>
    dbExecutionContext.withConnection { implicit connection =>
      val emotions = emotionRecordDao.findAllByUserId(userId)
      Future.successful(Ok(Json.toJson(emotions)))
    }
  }
}
