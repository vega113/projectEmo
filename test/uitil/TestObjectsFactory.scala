package uitil

import dao.model.{Emotion, EmotionRecord, Note, SubEmotion, Tag, Trigger}

object TestObjectsFactory {
  def createEmotionRecord(): EmotionRecord = {
    val subEmotions = List(SubEmotion(Option("Amusement"), Option("Amusement"), Option("description"), Option("Joy")))
    val triggers = List(Trigger(Option(1), Some("Person"), Some(1), Some(1), Some("Listening to music")))
    val notes = List(Note(Some(1), Some("Note 1"), "Note 1 description", None))
    val tags = List(Tag(Some(1), "Tag 1", None))
    EmotionRecord(Option(1), "Positive", Option(1L), Some(Emotion(Some("Joy"), Option("Joy"), Some("Positive"), Some("description"))), 5, subEmotions, triggers, notes, tags, None)
  }
}
