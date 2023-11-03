package controllers

import auth.model.{AuthenticatedRequest, TokenData}
import auth.{AuthenticatedAction, JwtService}
import play.api.mvc.{Request, Result}
import service.UserService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class MockAuthenticatedAction(userService: UserService, jwtService: JwtService) extends AuthenticatedAction(userService, jwtService) {
  override def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    Future.successful(Right(new AuthenticatedRequest(TokenData(1, "mockUser", "a@b,com", "a", "b"), request)))
  }
}
