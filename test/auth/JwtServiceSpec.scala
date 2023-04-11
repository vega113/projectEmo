package auth

import dao.model.User
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import play.api.mvc._
import play.api.test.Helpers
import service.DateTimeService

import java.time.{Clock, Instant, LocalDateTime, ZoneId}
import scala.concurrent.duration._

trait Data {
  val secret = "test-secret"
  val dateTimeServiceMock: DateTimeService = mock[DateTimeService]
  val jwtService = new JwtServiceImpl(dateTimeServiceMock)


  private val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
  when(dateTimeServiceMock.clock()).thenReturn(clock)
  when(dateTimeServiceMock.now()).thenReturn(LocalDateTime.now(clock))

  val parser: BodyParser[AnyContent] = Helpers.stubBodyParser(AnyContent("hello"))
}

class JwtServiceSpec extends PlaySpec with MockitoSugar {


  "JwtService" should {
    "create and validate a token successfully" in new Data {
      private val user = User(Some(1), "test-user", "test-password", Some("Test"), Some("User"), "testuser@test.com", isPasswordHashed = true)
      private val token = jwtService.createToken(user, 1.hour)

      jwtService.validateToken(token) mustBe Some(user.toTokenData)
    }

    "fail to validate an invalid token" in new Data {
      jwtService.validateToken("a.b.invalid-token") mustBe None
    }
  }
}

