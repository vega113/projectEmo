import {Component, OnInit} from '@angular/core';
import {MatDatepicker, MatDatepickerInputEvent} from '@angular/material/datepicker';
import {getDate, getDay, getMonth, getYear, isSameDay, setMonth, setYear} from 'date-fns';

import {DayOfWeek, EmotionRecord, Week} from "../models/emotion.model";
import {EmotionService} from "../services/emotion.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {DateService} from "../services/date.service";
import {ColorService} from "../services/color.service";



@Component({
  selector: 'app-emotion-calendar',
  templateUrl: './emotion-calendar.component.html',
  styleUrls: ['./emotion-calendar.component.css']
})
export class EmotionCalendarComponent implements OnInit {
  date = new Date();
  monthRecords: EmotionRecord[] | undefined;
  isLoadingRecords = false;
  calendarWeeks: Week[] = [];

  constructor(private emotionRecordService: EmotionService,
              private snackBar: MatSnackBar,
              private dateService: DateService,
              private colorService: ColorService
              ) { }

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

  findDayRecord(date: Date, monthRecords: EmotionRecord[] | undefined): DayOfWeek {
    const day = getDate(date);
    const month = getMonth(date);
    const year = getYear(date);

    if (monthRecords) {
      const dayRecords = monthRecords.filter(dayRecord => {
        const currentDay = new Date(year, month, day);
        return isSameDay(new Date(this.dateService.formatDateFromDb(dayRecord.created!)), currentDay);
      });

      if (dayRecords.length > 0) {
        const emotionSums = this.calculateIntensityPerEmotion(dayRecords);
        const averageIntensitiesPerType:{emotionType: string, averageIntensity: number}[] =
          this.calculateAverageIntensityPerEmotion(emotionSums);
        const averageIntensity = averageIntensitiesPerType.length > 0 ?
            averageIntensitiesPerType.reduce((sum, {averageIntensity}) => sum + averageIntensity, 0) / averageIntensitiesPerType.length : 0;

        const sortedEmotionSums = this.sortEmotionsSums(emotionSums);
        console.log("sortedEmotionSums for: " + dayRecords[0].created, sortedEmotionSums);
        let emotionType = sortedEmotionSums.length > 0 ? sortedEmotionSums[0].emotionType : 'Empty';

        return {
          date: day,
          dateTime: date,
          records: dayRecords,
          averageIntensity: averageIntensity,
          dayColor: this.colorService.getColorForEmotionType(emotionType, dayRecords.length > 0)
        };
      }
    }

    return {
      date: day,
      records: [],
      averageIntensity: 0,
      dayColor: this.colorService.getColorForEmotionType('', false)
    };
  }

  calculateIntensityPerEmotion(records: EmotionRecord[]): {[key: string]: {sum: number, count: number}} {
    let emotionSums: {[key: string]: {sum: number, count: number}} = {};

    records.forEach(record => {
      if (!emotionSums[record.emotionType]) {
        emotionSums[record.emotionType] = {sum: 0, count: 0};
      }
      emotionSums[record.emotionType].sum += record.intensity;
      emotionSums[record.emotionType].count++;
    });

    return emotionSums;
  }


  calculateAverageIntensityPerEmotion(emotionSums: {[key: string]: {sum: number, count: number}}): {emotionType: string, averageIntensity: number}[] {
    return Object.entries(emotionSums).map(([emotionType, {sum, count}]) => ({
      emotionType,
      averageIntensity: sum / count
    }));
  }

  sortEmotionsSums(emotionSums: {[key: string]: {sum: number, count: number}}): {emotionType: string, sum: number, count: number}[] {
  return Object.entries(emotionSums).map(([emotionType, {sum, count}]) => ({
    emotionType,
    sum,
    count
  })).sort((a, b) => b.sum - a.sum || b.count - a.count);
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
