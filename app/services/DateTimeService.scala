package services

import java.time.LocalDateTime

trait DateTimeService {
  def now(): LocalDateTime
}

class DateTimeServiceImpl extends DateTimeService {
  override def now(): LocalDateTime = LocalDateTime.now()
}
