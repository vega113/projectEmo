package controllers

import dao.{DatabaseExecutionContext, UserDao}
import dao.model.User
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import javax.inject.Inject
import scala.concurrent.Future
import play.api.libs.json._

class UserController @Inject()(cc: ControllerComponents, userDao: UserDao, dbExecutionContext: DatabaseExecutionContext)
  extends AbstractController(cc) {

  def getAllUsers: Action[AnyContent] = Action.async { implicit request =>
    dbExecutionContext.withConnection { implicit connection =>
      val users = userDao.findAll()
      Future.successful(Ok(Json.toJson(users)))
    }
  }

  def getUser(id: Int): Action[AnyContent] = Action.async { implicit request =>
    dbExecutionContext.withConnection { implicit connection =>
      userDao.findById(id) match {
        case Some(user) => Future.successful(Ok(Json.toJson(user)))
        case None => Future.successful(NotFound(s"User with id $id not found"))
      }
    }
  }

  def createUser: Action[AnyContent] = Action.async { implicit request =>
    request.body.asJson.map { json =>
      json.validate[User].map { user =>
        dbExecutionContext.withConnection { implicit connection =>
          userDao.insert(user) match {
            case Some(id) => Future.successful(Created(Json.toJson(user.copy(id = id.toInt))))
            case None => Future.successful(InternalServerError("Failed to create user"))
          }
        }
      }.recoverTotal { e =>
        Future.successful(BadRequest("Invalid user format"))
      }
    }.getOrElse {
      Future.successful(BadRequest("Expecting application/json request body"))
    }
  }

  def updateUser(id: Int): Action[AnyContent] = Action.async { implicit request =>
    request.body.asJson.map { json =>
      json.validate[User].map { user =>
        dbExecutionContext.withConnection { implicit connection =>
          userDao.update(user) match {
            case 1 => Future.successful(Ok(Json.toJson(user)))
            case _ => Future.successful(NotFound(s"User with id $id not found"))
          }
        }
      }.recoverTotal { e =>
        Future.successful(BadRequest("Invalid user format"))
      }
    }.getOrElse {
      Future.successful(BadRequest("Expecting application/json request body"))
    }
  }

  def deleteUser(id: Int): Action[AnyContent] = Action.async { implicit request =>
    dbExecutionContext.withConnection { implicit connection =>
      userDao.delete(id) match {
        case 1 => Future.successful(Ok(s"User with id $id deleted"))
        case _ => Future.successful(NotFound(s"User with id $id not found"))
      }
    }
  }
}
