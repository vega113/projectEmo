import {Component, Input} from '@angular/core';
import {DayOfWeek} from "../models/emotion.model";
import { DayInfoDialogComponent } from '../day-info-dialog/day-info-dialog.component';
import {MatDialog} from "@angular/material/dialog";

@Component({
  selector: 'app-day',
  templateUrl: './day.component.html',
  styleUrls: ['./day.component.css']
})
export class DayComponent {
  @Input() day: DayOfWeek | null = null;


  constructor(public dialog: MatDialog) {}

  showDayInfo() {
    this.dialog.open(DayInfoDialogComponent, {
      data: {
        day: this.day,
        getAverageIntensity: () => this.getAverageIntensity()
      },
      hasBackdrop: true,       // this will show a backdrop behind the dialog (default is true)
      disableClose: false
    });
  }


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

        if (record.subEmotions.length > 0) {
          info += ` Emotion: ${record.subEmotions[0].subEmotionName},`;
        }
        info += ` Intensity : ${record.intensity},`;

        // Assuming there is at most one SubEmotion and Trigger per EmotionRecord

        if (record.triggers.length > 0) {
          info += ` Trigger: ${record.triggers[0].triggerName},`;
        }

       if(record.notes.length > 0) {
          info += ` : ${record.notes[0]?.title}`;
        }
      }

      if (day.records.length > 5) {
        info += `There are more emotion records not displayed.\n`;
      }
      return info;
    }
    return '';
  }

  private hoverTimeout: any;

  onMouseEnter() {
    this.hoverTimeout = setTimeout(() => {
      this.showDayInfo();
    }, 500);
  }

  onMouseLeave() {
    clearTimeout(this.hoverTimeout);
    // Close the dialog here

  }
}
