package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json.{JsValue, Json}
import dao.model._
import dao.EmotionRecordWithRelationsDao
import play.api.db.DBApi

@Singleton
class EmotionRecordWithRelationsController @Inject()(dbApi: DBApi, cc: ControllerComponents) extends AbstractController(cc) {

  private val database = dbApi.database("default")
  private val emotionRecordWithRelationsDao = new EmotionRecordWithRelationsDao()

  def getAll: Action[AnyContent] = Action {
    database.withConnection { implicit connection =>
      val emotionRecords = emotionRecordWithRelationsDao.findAll()
      Ok(Json.toJson(emotionRecords))
    }
  }

  def getById(id: Int): Action[AnyContent] = Action {
    database.withConnection { implicit connection =>
      emotionRecordWithRelationsDao.findById(id) match {
        case Some(emotionRecord) => Ok(Json.toJson(emotionRecord))
        case None => NotFound
      }
    }
  }
  
  def insert: Action[JsValue] = Action(parse.json) { request =>
    val emotionRecordWithRelations = request.body.as[EmotionRecordWithRelations]
    database.withTransaction { implicit connection =>
      val id = emotionRecordWithRelationsDao.insert(emotionRecordWithRelations)
      id.map(newId => Created(Json.obj("id" -> newId))).getOrElse(InternalServerError)
    }
  }

  def update(id: Int): Action[JsValue] = Action(parse.json) { request =>
    val emotionRecordWithRelations = request.body.as[EmotionRecordWithRelations]
    database.withTransaction { implicit connection =>
      val updatedRows = emotionRecordWithRelationsDao.update(emotionRecordWithRelations)
      if (updatedRows > 0) NoContent else NotFound
    }
  }

  def delete(id: Int): Action[AnyContent] = Action {
    database.withTransaction { implicit connection =>
      val deletedRows = emotionRecordWithRelationsDao.delete(id)
      if (deletedRows > 0) NoContent else NotFound
    }
  }

  def getAllByUserId(userId: Int): Action[AnyContent] = Action {
    database.withConnection { implicit connection =>
      val emotionRecords = emotionRecordWithRelationsDao.findAllByUserId(userId)
      Ok(Json.toJson(emotionRecords))
    }
  }
}
