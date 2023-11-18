package service

import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

class TitleServiceImplSpec extends PlaySpec with MockitoSugar {
  "TitleServiceImplSpec" should {
    "makeTitle from a simple string" in {
      val titleService = new TitleServiceImpl
      val text = "This is a test title"
      val title = titleService.makeTitle(text)
      title mustBe "This is a test title"
    }

    "makeTitle From two sentences" in {
      val titleService = new TitleServiceImpl
      val text = "This is a test title. I like to test things."
      val title = titleService.makeTitle(text)
      title mustBe "This is a test title"
    }
  }
}
