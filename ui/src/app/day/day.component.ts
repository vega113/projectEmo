import {Component, Input} from '@angular/core';
import {DayOfWeek, EmotionRecord} from "../models/emotion.model";

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
}
