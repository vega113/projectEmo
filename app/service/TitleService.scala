package service

import com.google.inject.ImplementedBy

@ImplementedBy(classOf[TitleServiceImpl])
trait TitleService {
  def makeTitle(text: String): String
}

class TitleServiceImpl extends TitleService {
  def makeTitle(text: String): String = {
    val maxLength = 50
    val firstSegment = text.split("[.,:;\\-\n]")(0)
    val firstLine = firstSegment.replaceAll("\\[\\[|]]|#", "")
    if (firstLine.length > maxLength) {
      firstLine.substring(0, maxLength) + "..."
    } else {
      firstLine
    }
  }
}