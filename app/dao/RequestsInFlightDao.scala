package dao

import anorm.SQL
import dao.model.RequestsInFlight

import java.sql.Connection

class RequestsInFlightDao {
  def fetchRequestInFlight(idempotencyKey: String)(implicit connection: Connection): Option[RequestsInFlight] = {
    val requestsInFlight = SQL(
      """
        |SELECT * FROM requests_in_flight
        |WHERE request_id = {idempotencyKey} order by updated desc
        |""".stripMargin).on("idempotencyKey" -> idempotencyKey).as(RequestsInFlight.parser.*)
    requestsInFlight.headOption
  }

  def  createRequestInFlight(idempotencyKey: String)(implicit connection: Connection): Unit = {
    SQL(
      """
        |INSERT INTO requests_in_flight (request_id)
        |VALUES ({idempotencyKey})
        |""".stripMargin).on("idempotencyKey" -> idempotencyKey).executeUpdate()
  }

  def updateRequestInFlight(idempotencyKey: String)(implicit connection: Connection): Unit = {
    SQL(
      """
        |UPDATE requests_in_flight
        |SET updated = now()
        |WHERE request_id = {idempotencyKey}
        |""".stripMargin).on("idempotencyKey" -> idempotencyKey).executeUpdate()
  }

  def markRequestComplete(idempotencyKey: String)(implicit connection: Connection): Int = {
    val updatedCount = SQL(
      """
        |UPDATE requests_in_flight
        |SET is_completed = true, updated = now()
        |WHERE request_id = {idempotencyKey}
        |""".stripMargin).on("idempotencyKey" -> idempotencyKey).executeUpdate()
    updatedCount
  }
}
