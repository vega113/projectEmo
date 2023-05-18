import {Component, Input} from '@angular/core';
import {DayOfWeek} from "../models/emotion.model";

@Component({
  selector: 'app-day',
  templateUrl: './day.component.html',
  styleUrls: ['./day.component.css']
})
export class DayComponent {
  @Input() day: DayOfWeek | null = null;

  getColor() {
    return this.day?.dayColor
  }

  getAverageIntensity() {
    return this.day?.averageIntensity.toFixed(2)
  }

  getDayInfo(): string {
    if (this.day) {
      const day = this.day;
      let info = ``;

      for (let i = 0; i < day.records.length && i < 5; i++) {
        let record = day.records[i];

        info += `#${i + 1}:`;
        if(record.emotion){
          info += `Emotion: ${record.emotion.emotionName}\n`;
        }
        info += `Intensity: ${record.intensity}\n`;

        // Assuming there is at most one SubEmotion and Trigger per EmotionRecord
        if (record.subEmotions.length > 0) {
          info += `Sub-Emotion: ${record.subEmotions[0].subEmotionName}\n`;
        }
        if (record.triggers.length > 0) {
          info += `Trigger: ${record.triggers[0].triggerName}\n`;
        }

        if(record.created) {
          info += `Created: ${new Date(record.created).toLocaleString()}\n\n`;
        }
      }

      if (day.records.length > 5) {
        info += `There are more emotion records not displayed.\n`;
      }
      return info;
    }
    return '';
  }
}
