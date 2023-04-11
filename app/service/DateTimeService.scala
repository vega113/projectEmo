package service

import com.google.inject.ImplementedBy

import java.time.{Clock, LocalDateTime}

@ImplementedBy(classOf[DateTimeServiceImpl])
trait DateTimeService {
  def now(): LocalDateTime
  def clock(): Clock
}

class DateTimeServiceImpl extends DateTimeService {
  override def now(): LocalDateTime = LocalDateTime.now()

  def clock(): Clock = {
    java.time.Clock.systemUTC()
  }
}
