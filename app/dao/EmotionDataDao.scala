package dao

import controllers.model.EmotionData

import java.sql.Connection
import javax.inject.Inject

class EmotionDataDao @Inject()(emotionDao: EmotionDao,
                               subEmotionDao: SubEmotionDao,
                               triggerDao: TriggerDao
                              ){
  def fetchEmotionData()(implicit connection: Connection): EmotionData = {
   // fetch all emotions and sub-emotions and suggested actions and triggers
  ???
  }
}
