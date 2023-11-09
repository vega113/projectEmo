package service

import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

import java.time.ZonedDateTime
import scala.util.{Success, Try}

class DateTimeServiceImplSpec extends PlaySpec with MockitoSugar {
  trait TestData {
    val dateTimeService = new DateTimeServiceImpl()
  }

  "DateTimeServiceImpl" should {
    "parse date range correctly" in new TestData {
      val monthStart = "2023-10-01T00:00:00+03:00"
      val monthEnd = "2023-10-31T23:59:59+02:00"
      val actual: Try[(ZonedDateTime, ZonedDateTime)] = dateTimeService.parseMonthRange(Option(monthStart),
        Option(monthEnd))
      val expected: Success[(ZonedDateTime, ZonedDateTime)] = Success((ZonedDateTime.parse(monthStart),
        ZonedDateTime.parse(monthEnd)))
      actual mustEqual expected
    }
  }
}
