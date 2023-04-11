package auth

import auth.model.{AuthenticatedRequest, TokenData}
import dao.model.User
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Results.Unauthorized
import play.api.mvc._
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import play.api.test.{FakeHeaders, FakeRequest}
import service.UserService

import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedActionSpec extends PlaySpec with MockitoSugar {
  val mockUserService: UserService = mock[UserService]
  val mockJwtService: JwtService = mock[JwtService]
  implicit val mockExecutionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  class TestAuthenticatedAction extends AuthenticatedAction(mockUserService, mockJwtService)

  val testAuthenticatedAction = new TestAuthenticatedAction()

  val token = "Bearer testToken"
  val validToken = "Bearer validToken"
  val invalidToken = "invalidToken"

  private val fakeUser = User(Option(1L), "fakeUserName", "fakePassword", Option("fakeFirstName"), Option("fakeLastName"),  "fake_email@gmail.com", isPasswordHashed = false)
  val fakeTokenData: TokenData = fakeUser.toTokenData

  "AuthenticatedAction" should {
    "return Unauthorized when no token is present in the headers" in {
      val request: Request[AnyContent] = FakeRequest("GET", "/", FakeHeaders(), AnyContentAsEmpty)
      val result: Future[Result] = testAuthenticatedAction.invokeBlock(request, (_: AuthenticatedRequest[AnyContent]) => Future.successful(Results.Ok))
      val response: Result = await(result)
      response mustBe Unauthorized("Authorization token not found or invalid")
    }

    "return Unauthorized when an invalid token is present in the headers" in {
      val request: Request[AnyContent] = FakeRequest("GET", "/", FakeHeaders(Seq("Authorization" -> invalidToken)), AnyContentAsEmpty)
      when(mockJwtService.validateToken(invalidToken)).thenReturn(None)
      val result: Future[Result] = testAuthenticatedAction.invokeBlock(request, (_: AuthenticatedRequest[AnyContent]) => Future.successful(Results.Ok))
      val response: Result = await(result)
      response mustBe Unauthorized("Authorization token not found or invalid")
    }

    "return Ok when a valid token is present in the headers" in {
      val request: Request[AnyContent] = FakeRequest("GET", "/", FakeHeaders(Seq("Authorization" -> validToken)), AnyContentAsEmpty)
      when(mockJwtService.validateToken(any())).thenReturn(Some(fakeTokenData))
      when(mockUserService.findByUsername(fakeUser.username)).thenReturn(Future.successful(Some(fakeUser)))
      val result: Future[Result] = testAuthenticatedAction.invokeBlock(request, (_: AuthenticatedRequest[AnyContent]) => Future.successful(Results.Ok))
      val response: Result = await(result)
      response mustBe Results.Ok
    }
  }
}
