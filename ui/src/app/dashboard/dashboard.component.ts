import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { EmotionService } from "../services/emotion.service";
import { MatSnackBar } from "@angular/material/snack-bar";
import { DateService } from "../services/date.service";
import {SunburstData} from "../models/emotion.model";
import {FormControl} from "@angular/forms";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  chartData: SunburstData[] | null = null;
  isLoading: boolean = false;
  activeTab: number = 0; // Variable to store the index of the active tab

  startDate: Date | null = null;
  endDate: Date | null = null;

  startDateControl = new FormControl();
  endDateControl = new FormControl();

  constructor(
    private http: HttpClient,
    private emotionService: EmotionService,
    private snackBar: MatSnackBar,
    private dateService: DateService
  ) {}

  ngOnInit() {
    const dateRange = this.dateService.createThreeMonthDateRange();
    if(! this.startDate) {
      this.startDate = dateRange.start;
    }
    if(! this.endDate) {
      this.endDate = dateRange.end;
    }

    this.startDateControl.valueChanges.subscribe(value => {
      this.startDate = value;
      console.log('startDateControl value changed:', value);
    });

    this.endDateControl.valueChanges.subscribe(value => {
      this.endDate = value;
      console.log('endDateControl value changed:', value);
    });
    this.fetchChartData();
  }

  fetchChartData() {
    this.isLoading = true;


    const dateRangeStr = {
      start: this.dateService.formatDateToIsoString(this.startDate!),
      end: this.dateService.formatDateToIsoString(this.endDate!)
    }
    this.emotionService.fetchEmotionSunburnChartDataForDateRange(dateRangeStr).subscribe({
      next: (response) => {
        this.chartData = response;
        this.isLoading = false;
        console.log('EmotionSunburnChartData received:', response);
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Error getting EmotionSunburnChartData', error);
        this.snackBar.open('Error getting chart data', 'Close', {
          duration: 5000,
        });
      }
    });
  }

  setActiveTab(index: number) {
    this.activeTab = index;
  }
}
