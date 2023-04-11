package auth

import dao.model.User
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, AnyContentAsEmpty, Request, Result, Results}
import play.api.test.FakeRequest

import scala.concurrent.{Await, Future}

class JwtAuthActionSpec extends PlaySpec with MockitoSugar {

  "JwtAuthAction" should {
    "invoke block with authenticated request if valid token is provided" in new Data {
      val user: User = User(Some(1), "test-user", "test-password", Some("Test"), Some("User"), "testuser@test.com",  isPasswordHashed = true)
      val token: String = jwtService.createToken(user, 1.hour)
      val request = new AuthenticatedRequest(user.toTokenData, FakeRequest().withHeaders("Authorization" -> s"Bearer $token"))


      val actual: Future[Result] = authAction.invokeBlock[AnyContent](request, { req: Request[AnyContent] =>
        Future.successful(Results.Ok(Json.toJson( req.asInstanceOf[AuthenticatedRequest[AnyContent]].user)))
      })
      Await.result(actual, 1.second) mustBe Results.Ok(Json.toJson(user.toTokenData))
    }

    "return unauthorized result if no authorization header is provided" in new Data {
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

      val actual: Future[Result] = authAction.invokeBlock(request, { _: Request[AnyContent] =>
        Future.successful(Results.Ok)
      })
      Await.result(actual, 1.second) mustBe Results.Unauthorized("Authorization token not found")
    }

    "return unauthorized result if invalid token is provided" in new Data {
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders("Authorization" -> "Bearer invalid-token")

      val actual: Future[Result] = authAction.invokeBlock(request, { _: Request[AnyContent] =>
        Future.successful(Results.Ok)
      })
      Await.result(actual, 1.second) mustBe Results.Unauthorized("Invalid token")
    }
  }

}
