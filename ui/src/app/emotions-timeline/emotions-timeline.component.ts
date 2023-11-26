import { Component, OnInit, ViewChild, AfterViewInit, ChangeDetectorRef } from '@angular/core';
import { EmotionService } from '../services/emotion.service';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { Router } from '@angular/router';
import { EmotionStateService } from '../services/emotion-state.service';
import {MatSort} from "@angular/material/sort";
import {EmotionRecord} from "../models/emotion.model";
import {MatSnackBar} from "@angular/material/snack-bar";


@Component({
  selector: 'app-emotions-timeline',
  templateUrl: './emotions-timeline.component.html',
  styleUrls: ['./emotions-timeline.component.css'],
})
export class EmotionsTimelineComponent implements OnInit, AfterViewInit {
  displayedColumns: string[] = [
    'date',
    'intensity',
    'subEmotion',
    'noteTitle',
    'actions'
  ];
  dataSource: MatTableDataSource<EmotionRecord>;

  @ViewChild(MatPaginator, { static: false }) paginator!: MatPaginator;

  @ViewChild(MatSort) sort!: MatSort;

  isLoading: boolean = true;

  constructor(
      private emotionService: EmotionService,
      private router: Router,
      private emotionStateService: EmotionStateService,
      private changeDetector: ChangeDetectorRef,
      private snackbar: MatSnackBar
  ) {
    this.dataSource = new MatTableDataSource();
  }

  ngOnInit(): void {
    this.emotionService.fetchEmotionRecordsForCurrentUser().subscribe((data) => {
      this.dataSource.data = data;
      this.dataSource.paginator = this.paginator;
      this.isLoading = false;
    });
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
    this.dataSource.sort.active = 'date';
    this.dataSource.sort.direction = 'desc';
    this.dataSource.sortingDataAccessor = (data, sortHeaderId) => {
      switch (sortHeaderId) {
        case 'date':
          return this.getDateForSorting(data);
        case 'subEmotion':
          return data.subEmotions[0]?.subEmotionName || '';
        case 'noteTitle':
          return data.notes[0]?.title || '';
          case 'intensity':
            return data.intensity;
        default:
          return this.getDateForSorting(data);
      }
    };
    this.changeDetector.detectChanges();
  }

  onRowClicked(record: any): void {
    this.emotionStateService.updateNewEmotion(record); // Save the selected emotion record in the state
    this.router.navigate(['/display-emotion']);
  }

  getDateForSorting(data: any): number {
    return new Date(data.created).getTime();
  }

  onDelete(record: EmotionRecord): void {
    this.emotionService.deleteEmotionRecord(record.id!).subscribe({
        next: () => {
            this.snackbar.open(`Emotion record deleted successfully: ${record.notes[0].title}` , 'Close', {
            duration: 3000,
            });
            this.ngOnInit();
        },
        error: (err) => {
            console.error(err);
            this.snackbar.open('Error deleting emotion record', 'Close', {
            duration: 3000,
            });
        },
    });
  }
}
