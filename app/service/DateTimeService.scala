package service

import com.google.inject.ImplementedBy
import play.api.Logger

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDateTime, YearMonth, ZonedDateTime}
import scala.util.{Failure, Success, Try}

@ImplementedBy(classOf[DateTimeServiceImpl])
trait DateTimeService {
  def now(): LocalDateTime
  def clock(): Clock

  def parseMonthRange(monthStartOpt: Option[String], monthEndOpt: Option[String]): Try[(ZonedDateTime, ZonedDateTime)]
}

class DateTimeServiceImpl extends DateTimeService {
  val logger: Logger = play.api.Logger(getClass)
  override def now(): LocalDateTime = LocalDateTime.now()

  def clock(): Clock = {
    java.time.Clock.systemUTC()
  }

  override def parseMonthRange(monthStartOpt: Option[String], monthEndOpt: Option[String]): Try[(ZonedDateTime, ZonedDateTime)] = {
    val now = ZonedDateTime.now()
    val zoneId = now.getZone
    val firstDayOfMonth: ZonedDateTime = YearMonth.from(now).atDay(1).atStartOfDay(now.getZone)
    val lastDayOfMonth: ZonedDateTime = YearMonth.from(now).atEndOfMonth().atTime(23, 59, 59).atZone(zoneId)
    try {
      val monthStart = monthStartOpt.map(s => ZonedDateTime.parse(s)).getOrElse(firstDayOfMonth)
      val monthEnd = monthEndOpt.map(s => ZonedDateTime.parse(s)).getOrElse(lastDayOfMonth)
      Success(monthStart, monthEnd)
    } catch {
      case e: Exception =>
        logger.error(s"Error parsing month range, monthStartOpt: $monthStartOpt, monthEndOpt: $monthEndOpt. " +
          s"Returns default: firstDayOfMonth: $firstDayOfMonth, lastDayOfMonth: $lastDayOfMonth", e)
        Failure(e)
    }
  }
}
