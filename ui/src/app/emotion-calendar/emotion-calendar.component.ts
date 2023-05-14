import {Component, OnInit} from '@angular/core';
import {MatDatepicker, MatDatepickerInputEvent} from '@angular/material/datepicker';
import {getDate, getDay, getMonth, getYear, isSameDay, setMonth, setYear} from 'date-fns';

import {DayOfWeek, EmotionRecord, EmotionRecordMonth, Week} from "../models/emotion.model";
import {EmotionService} from "../services/emotion.service";
import {MatSnackBar} from "@angular/material/snack-bar";


@Component({
  selector: 'app-emotion-calendar',
  templateUrl: './emotion-calendar.component.html',
  styleUrls: ['./emotion-calendar.component.css']
})
export class EmotionCalendarComponent implements OnInit {
  date = new Date();
  monthRecords: EmotionRecordMonth | undefined;
  isLoadingRecords = false;
  calendarWeeks: Week[] = [];

  constructor(private emotionRecordService: EmotionService, private snackBar: MatSnackBar) { }

  ngOnInit() {
    this.fetchRecords(this.date);
  }

  fetchRecords(month: Date) {
    this.isLoadingRecords = true;
    this.emotionRecordService.fetchMonthEmotionRecordsForCurrentUser(month).subscribe({
      next: records => {
        this.monthRecords = records;
        this.buildCalendar(this.date);
      },
      error: err => {
        console.error(err);
        this.isLoadingRecords = false;
        this.snackBar.open('Error getting emotion record data for calendar', 'Close', {
          duration: 5000,
        });
      },
      complete: () => {
        this.isLoadingRecords = false;
        console.log('Fetching of records completed');
      }
    });
  }

  monthSelected(event: MatDatepickerInputEvent<Date>) {
    if (event.value !== null) {
      this.date = event.value;
    }
    this.fetchRecords(this.date);
  }

  buildCalendar(date: Date) {
    const year = getYear(date);
    const month = getMonth(date);

    const firstDayOfMonth = new Date(year, month, 1);
    const lastDayOfMonth = new Date(year, month + 1, 0);

    const weeks: Week[] = [];
    let week: DayOfWeek[] = [];

    // Padding for days before the start of the month
    for (let i = 0; i < getDay(firstDayOfMonth); i++) {
      week.push({
        date: 0,
        records: [],
        averageIntensity: 0,
        dayColor: 'white'
      });
    }

    for (let day = 1; day <= getDate(lastDayOfMonth); day++) {
      const date = new Date(year, month, day);

      const dayRecord: DayOfWeek = this.findDayRecord(date, this.monthRecords);

      week.push(dayRecord);

      if (getDay(date) === 6 || day === getDate(lastDayOfMonth)) {
        // The week is over, or this is the end of the month
        weeks.push({
          days: week
        });
        week = [];
      }
    }

    this.calendarWeeks = weeks;
  }

  findDayRecord(date: Date, monthRecords: EmotionRecordMonth | undefined): DayOfWeek {
    const day = getDate(date);
    const month = getMonth(date);
    const year = getYear(date);

    if (monthRecords) {
      const dayRecord = monthRecords.weeks.map(week => week.days).flat().find(dayRecord => {
        const currentDay = new Date(year, month, day);
        return isSameDay(dayRecord.date, currentDay);
      });

      if (dayRecord) {
        const averageIntensity = this.calculateAverageIntensity(dayRecord.records);
        return {
          date: day,
          records: dayRecord.records,
          averageIntensity: averageIntensity,
          dayColor: this.getColorForDay(averageIntensity)
        };
      }
    }

    return {
      date: day,
      records: [],
      averageIntensity: 0,
      dayColor: 'grey'
    };
  }



  getColorForDay(averageIntensity: number) {

    let color = 'blue';
    if(averageIntensity < 0) {
      color = 'red';
    } else if (averageIntensity > 0) {
      color = 'green';
    } else {
      color = 'grey';
    }
    return color;
  }

  calculateAverageIntensity(records: EmotionRecord[]): number {
    return records.length > 0 ? records.reduce((a, b) =>
      a + b.intensity * this.mapEmotionTypeToMultiplier(b.emotionType), 0) / records.length : 0;
  }

  private mapEmotionTypeToMultiplier(emotionType: string): number {
    if (emotionType === 'Positive') {
      return 1;
    } else if(emotionType === 'Negative') {
      return -1;
    } else {
      return 0;
    }
  }

  chosenYearHandler(normalizedYear: Date) {
    this.date = setYear(this.date, getYear(normalizedYear));
  }

  chosenMonthHandler(normalizedMonth: Date, datepicker: MatDatepicker<Date>) {
    this.date = setMonth(this.date, getMonth(normalizedMonth));
    datepicker.close();
    this.fetchRecords(normalizedMonth)
  }
}
