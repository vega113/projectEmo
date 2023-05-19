import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { EmotionService } from "../services/emotion.service";
import { MatSnackBar } from "@angular/material/snack-bar";
import { DateService } from "../services/date.service";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  chartData: Map<string, Map<string, Map<string, number>>> | null = null;
  isLoading: boolean = false;
  activeTab: number = 0; // Variable to store the index of the active tab

  constructor(
    private http: HttpClient,
    private emotionService: EmotionService,
    private snackBar: MatSnackBar,
    private dateService: DateService
  ) {}

  ngOnInit() {
    this.fetchChartData();
  }

  fetchChartData() {
    this.isLoading = true;
    const dateRange = this.dateService.createThreeMonthDateRange();
    this.emotionService.fetchEmotionSunburnChartDataForDateRange(dateRange).subscribe({
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
