package auth

import auth.model.TokenData
import com.google.inject.{ImplementedBy, Inject}
import com.typesafe.config.ConfigFactory
import dao.model.User
import org.slf4j.LoggerFactory
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtJson}
import play.api.libs.json.Json
import service.DateTimeService

import java.time.Clock
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}


@ImplementedBy(classOf[JwtServiceImpl])
trait JwtService {
  def createToken(user: User, expiration: Duration): String

  def validateToken(token: String): Option[TokenData]
}

class JwtServiceImpl @Inject()(dateTimeService: DateTimeService) extends JwtService {

  private lazy val clock: Clock = dateTimeService.clock()
  private val algorithm = JwtAlgorithm.HS256
  private val secretKey = ConfigFactory.load().getString("jwt.secret")
  private val logger = LoggerFactory.getLogger(classOf[JwtService])

  def createToken(user: User, expiration: Duration): String = {
    val claim = JwtClaim(
      content = Json.toJson(user.toTokenData).toString(),
      issuedAt = Option(clock.instant().getEpochSecond),
      expiration = Option(clock.instant().plusSeconds(expiration.toSeconds).getEpochSecond)
    )
    JwtJson.encode(claim, secretKey, algorithm)
  }

  def validateToken(token: String): Option[TokenData] = {
    Try {
      JwtJson.decodeJson(token, secretKey, Seq(algorithm))
        .flatMap { json =>
          json.validate[TokenData].asEither match {
            case Right(user) => Success(user)
            case Left(errors) => Failure(new RuntimeException(errors.map(error =>
              s"path: ${error._1}, errors: ${error._2}").mkString(",")))
          }
        } match {
        case Success(user) => Some(user)
        case Failure(failure) =>
          logger.warn(s"Error validating token: ${token}", failure.getMessage)
          None
      }
    } match {
      case Success(userOpt) => userOpt
      case Failure(failure) =>
        logger.error("Error validating token", failure)
        None
    }
  }
}
