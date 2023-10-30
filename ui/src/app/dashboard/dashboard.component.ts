import {Component, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {EmotionService} from "../services/emotion.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {DateService} from "../services/date.service";
import {
    EmotionTypesTriggersDoughnutData,
    LineChartData,
    LineChartTrendDataRow,
    LineChartTrendDataSet
} from "../models/emotion.model";
import {FormControl} from "@angular/forms";
import {BreakpointObserver, Breakpoints} from "@angular/cdk/layout";


@Component({
    selector: 'app-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
    isLoading: boolean = true;
    activeTab: number = 0; // Variable to store the index of the active tab

    startDate: Date | null = null;
    endDate: Date | null = null;

    startDateControl = new FormControl();
    endDateControl = new FormControl();

    doughnutEmotionTypeRecordCountChartOptions: any;
    doughnutEmotionTypeIntensityChartOptions: any;
    doughnutTriggersRecordCountChartOptions: any;
    doughnutTriggersIntensityChartOptions: any;

    seriesName: 'Distribution' | 'Trend' = 'Distribution';
    seriesType: 'distribution' | 'trend' = 'distribution';
    chartType: 'emotion' | 'trigger' = 'emotion'; // 'emotion' or 'trigger'
    plotType: 'recordCount' | 'intensity' = 'recordCount'; // 'recordCount' or 'intensity'

    isMobile: boolean | undefined;

    lineChartEmotionTypeCountOptions: any;
    lineChartEmotionTypeIntensityOptions: any;
    lineChartTriggerCountOptions: any;
    lineChartTriggerIntensityOptions: any;


    constructor(
        private emotionService: EmotionService,
        private snackBar: MatSnackBar,
        private dateService: DateService,
        private breakpointObserver: BreakpointObserver
    ) {
    }

    ngOnInit() {
        this.breakpointObserver.observe([
            Breakpoints.Handset
        ]).subscribe(result => {
            this.isMobile = result.matches;
        });
        this.initDateRange();
        this.subscribeToDateChanges();
        this.fetchChartData();
    }

    initDateRange(): void {
        const dateRange = this.dateService.createThreeMonthDateRange();
        this.startDate = this.startDate || dateRange.start;
        this.endDate = this.endDate || dateRange.end;
    }

    subscribeToDateChanges(): void {
        this.startDateControl.valueChanges.subscribe(value => {
            this.startDate = value;
            console.log('startDateControl value changed:', value);
        });
        this.endDateControl.valueChanges.subscribe(value => {
            this.endDate = value;
            console.log('endDateControl value changed:', value);
        });
    }

    fetchChartData(): void {
        this.isLoading = true;
        const dateRangeStr = {
            start: this.dateService.formatDateToIsoString(this.startDate!),
            end: this.dateService.formatDateToIsoString(this.endDate!)
        };

        this.getEmotionTypeTriggersChartData(dateRangeStr);
        this.getEmotionTypeTriggersTrendsChartData(dateRangeStr);
    }

    getEmotionTypeTriggersChartData(dateRange: any): void {
        this.emotionService.fetchEmotionDoughnutEmotionTypeTriggersChartDataForDateRange(dateRange).subscribe({
            next: (response: EmotionTypesTriggersDoughnutData) => {
                this.handleEmotionTypeChartData(response);
                this.handleEmotionTriggerChartData(response);
            },
            error: (error) => this.handleError('Error getting EmotionDoughnutEmotionTypeChartData', error)
        });
    }

    getEmotionTypeTriggersTrendsChartData(dateRange: any): void {
        console.log('getEmotionTypeTriggersTrendsChartData', dateRange);
        this.emotionService.fetchMonthEmotionRecordsByDayForCurrentUser(dateRange).subscribe({
            next: (response: LineChartTrendDataSet) => {
                console.log('LineChartTrendDataSet[] received:', response);
                this.handleEmotionTypeTrendChartData(response);
                this.handleEmotionTriggerTrendChartData(response);
            },
            error: (error) => this.handleError('Error getting EmotionDoughnutEmotionTypeChartData', error)
        });
    }

    handleEmotionTypeChartData(response: EmotionTypesTriggersDoughnutData): void {
        this.doughnutEmotionTypeRecordCountChartOptions = this.createDoughnutChartOptions(response.emotionTypes,
            "Emotion Categories Breakdown: Record Count", 'recordsCount');
        this.doughnutEmotionTypeIntensityChartOptions = this.createDoughnutChartOptions(response.emotionTypes,
            "Emotion Categories Breakdown: Intensity", 'intensitySum');
        this.isLoading = false;
        console.log('EmotionDoughnutEmotionTypeChartData received:', response);
    }


    handleEmotionTriggerChartData(response: EmotionTypesTriggersDoughnutData): void {
        this.doughnutTriggersRecordCountChartOptions = this.createDoughnutChartOptions(response.triggers,
            "Reasons Breakdown: Record Count", 'recordsCount');
        this.doughnutTriggersIntensityChartOptions = this.createDoughnutChartOptions(response.triggers,
            "Reasons Breakdown: Intensity", 'intensitySum');
        this.isLoading = false;
        console.log('EmotionDoughnutTriggerChartData received:', response);
    }

    createDoughnutChartOptions(data: any[], title: string, dataProperty: string): any {
        return {
            animationEnabled: true,
            title: {text: title, fontWeight: "normal"},
            data: [{
                type: "doughnut",
                yValueFormatString: "#,###.##'%'",
                indexLabel: "{name}",
                dataPoints: data.map(dp => ({
                    y: dp[dataProperty],
                    name: dp.name,
                    color: dp.color
                }))
            }]
        };
    }


    private handleEmotionTriggerTrendChartData(response: LineChartTrendDataSet) {
        this.lineChartTriggerCountOptions = this.createLineChartOptions(response, "Reasons Trend: Record Count",  response.triggerTypes, "triggersAccumulated", 'recordsCount', "Record Count");
        this.lineChartTriggerIntensityOptions = this.createLineChartOptions(response, "Reasons Trend: Intensity", response.triggerTypes, "triggersAccumulated", 'intensitySum', "Intensity");
        console.log('lineChartTriggerCountOptions', this.lineChartTriggerCountOptions);
        console.log('lineChartTriggerIntensityOptions', this.lineChartTriggerIntensityOptions);
    }

    private handleEmotionTypeTrendChartData(response: LineChartTrendDataSet) {
        this.lineChartEmotionTypeCountOptions = this.createLineChartOptions(response, "Emotion Categories Trend: Record Count", response.emotionTypes, "emotionTypeAccumulated", 'recordsCount', "Record Count");
        this.lineChartEmotionTypeIntensityOptions = this.createLineChartOptions(response, "Emotion Categories Trend: Intensity Count", response.emotionTypes, "emotionTypeAccumulated", 'intensitySum', "Intensity");
        console.log('lineChartEmotionTypeCountOptions', this.lineChartTriggerCountOptions);
        console.log('lineChartEmotionTypeIntensityOptions', this.lineChartTriggerIntensityOptions);
    }

    createLineChartOptions(data: LineChartTrendDataSet, title: string, objectTypes: string[], chartType: string, dataProperty: string, axisYTitle: string): any {
        function mapDataPoints(data: LineChartTrendDataRow[], chartType: string,
                               mapType: string, dataProperty: keyof LineChartData): any[] {
            return data.map(dp => {
                const dataset =
                    chartType === 'emotionTypeAccumulated' ? dp.emotionTypeAccumulated : dp.triggersAccumulated;
                const yValue = dataset[mapType]?.[dataProperty]*10 || 0;
                return {x: new Date(dp.date), y: yValue};
            });
        }

        return {
            animationEnabled: true,
            theme: "light2",
            title: {
                text: title
            },
            axisX: {
                valueFormatString: "DD-MMM-YYYY",
                intervalType: "day",
                interval: 3,
            },
            axisY: {
                title: axisYTitle,
            },
            toolTip: {
                shared: true
            },
            legend: {
                cursor: "pointer",
                itemclick: function (e: any) {
                    e.dataSeries.visible = !(typeof (e.dataSeries.visible) === "undefined" || e.dataSeries.visible);
                    e.chart.render();
                }
            },
            data: objectTypes.map((mapType: string) => {
                return {
                    type: "line",
                    name: mapType,
                    showInLegend: true,
                    color: data.colors[mapType],
                    dataPoints: mapDataPoints(data.rows, chartType, mapType, dataProperty as keyof LineChartData)
                }
            })
        }
    }


    handleError(message: string, error: any): void {
        this.isLoading = false;
        console.error(message, error);
        this.snackBar.open(message, 'Close', {duration: 5000});
    }

    setActiveTab(index: number): void {
        this.activeTab = index;
    }

    onToggleSeriesType() {
        console.log('this.seriesType', this.seriesType);
        console.log('this.chartType', this.chartType);
        console.log('this.plotType', this.plotType);
        if (this.seriesType === 'trend') {
            this.seriesName = 'Trend';
        } else {
            this.seriesName = 'Distribution';
        }
    }
}
