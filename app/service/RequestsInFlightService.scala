package service

import com.google.inject.ImplementedBy
import dao.model.RequestsInFlight
import dao.{DatabaseExecutionContext, RequestsInFlightDao}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[RequestsInFlightServiceImpl])
trait RequestsInFlightService {
  def fetchOrCreateRequestInFlight(idempotencyKey: String): Future[Option[RequestsInFlight]]
  def markRequestComplete(idempotencyKey: String): Future[Unit]
}

class RequestsInFlightServiceImpl @Inject()(databaseExecutionContext: DatabaseExecutionContext,
  requestsInFlightDao: RequestsInFlightDao)
  extends RequestsInFlightService {

  private lazy val logger = play.api.Logger(getClass)
  override def fetchOrCreateRequestInFlight(idempotencyKey: String): Future[Option[RequestsInFlight]] = {
    logger.info(s"Checking if request is in flight: idempotencyKey: $idempotencyKey")
    databaseExecutionContext.withConnection({ implicit connection =>
      Future.successful{
        requestsInFlightDao.fetchRequestInFlight(idempotencyKey) match {
          case requestsInFlightOpt@Some(requestsInFlight) =>
            if (requestsInFlight.isCompleted) {
              logger.info(s"Request is completed: idempotencyKey: $idempotencyKey")
            } else {
              logger.info(s"Request is not completed: idempotencyKey: $idempotencyKey")
              requestsInFlightDao.updateRequestInFlight(idempotencyKey)
            }
            requestsInFlightOpt
          case None =>
            logger.info(s"No request in flight for: idempotencyKey: $idempotencyKey")
            requestsInFlightDao.createRequestInFlight(idempotencyKey)
            None
        }
      }
    })
  }

  override def markRequestComplete(idempotencyKey: String): Future[Unit] = {
    logger.info(s"Marking request complete: idempotencyKey: $idempotencyKey")
    databaseExecutionContext.withConnection({ implicit connection =>
      Future.successful{
        requestsInFlightDao.markRequestComplete(idempotencyKey) match {
          case 0 =>
            logger.info(s"Failed to mark request complete: idempotencyKey: $idempotencyKey")
            throw new Exception(s"Failed to mark request complete: idempotencyKey: $idempotencyKey")
          case _ =>
            logger.info(s"Marked request complete: idempotencyKey: $idempotencyKey")
        }
      }
    })
  }
}
