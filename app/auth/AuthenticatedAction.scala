package auth

import auth.model.{AuthenticatedRequest, TokenData}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc._
import service.UserService

class AuthenticatedAction @Inject()(userService: UserService, jwtService: JwtService)(implicit ec: ExecutionContext)
  extends ActionRefiner[Request, AuthenticatedRequest] {

  def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    request.headers.get("Authorization") match {
      case Some(token) if token.startsWith("Bearer ") =>
        jwtService.validateToken(token.stripPrefix("Bearer ")) match {
          case Some(user) =>
            userService.findByUsername(user.username) flatMap  {
              case Some(user) => Future.successful(Right(new AuthenticatedRequest(user.toTokenData, request)))
              case None => Future.successful(Left(Results.Unauthorized("Invalid token")))
            }
          case _ => Future.successful(Left(Results.Unauthorized("Invalid token")))
        }
      case _ =>
        Future.successful(Left(Results.Unauthorized("Authorization token not found or invalid")))
    }
  }

  // use global execution context
  override protected def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
}



