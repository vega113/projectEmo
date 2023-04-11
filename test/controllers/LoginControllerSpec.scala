package controllers

import auth.JwtService
import auth.model.LoginData
import dao.model.User
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.UserService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class LoginControllerSpec extends PlaySpec with MockitoSugar {


  private val mockUserService = mock[UserService]
  private val mockJwtService = mock[JwtService]

  "LoginController" should {
    "return a token for a valid user" in {
      val loginData = LoginData("test-user", "test-password")
      val user = User(Some(1), "test-user", "test-password", Some("Test"), Some("User"), "testuser@test.com", isPasswordHashed = true)
      val token = "test-token"

      when(mockUserService.findByUsername(loginData.username)).thenReturn(Future.successful(Some(user)))
      when(mockJwtService.createToken(user, 1.hour)).thenReturn(token)

      val controller = new LoginController(mockUserService, stubControllerComponents(), mockJwtService)
      val fakeRequest = FakeRequest(GET, "/login").withBody(loginData)
      val login = controller.login().apply(fakeRequest)

      Await.result(login, 1.second).header.status mustEqual OK


    }

    "return a bad request for an invalid user" in {
      val loginData = LoginData("test-user", "test-password")

      when(mockUserService.findByUsername(loginData.username)).thenReturn(Future.successful(None))

      val controller = new LoginController(mockUserService, stubControllerComponents(), mockJwtService)
      val fakeRequest = FakeRequest(GET, "/login").withBody(loginData)
      val login = controller.login().apply(fakeRequest)

      val result = Await.result(login, 1.second)
      result.header.status mustEqual BAD_REQUEST
      contentAsJson(login) mustEqual Json.obj("message" -> "Invalid username or password")
    }

    "return a bad request for an invalid password" in {
      val loginData = LoginData("test-user", "test-password")
      val user = User(Some(1), "test-user", "invalid-password", Some("Test"), Some("User"), "testuser@test.com", isPasswordHashed = true)

      when(mockUserService.findByUsername(loginData.username)).thenReturn(Future.successful(Some(user)))

      val controller = new LoginController(mockUserService, stubControllerComponents(), mockJwtService)
      val fakeRequest = FakeRequest(GET, "/login").withBody(loginData)
      val login = controller.login().apply(fakeRequest)

      val result = Await.result(login, 1.second)
      result.header.status mustEqual BAD_REQUEST
      contentAsJson(login) mustEqual Json.obj("message" -> "Invalid username or password")
    }
  }
}

