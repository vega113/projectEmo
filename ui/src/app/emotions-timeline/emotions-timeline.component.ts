import {Component, OnInit, ViewChild, AfterViewInit, ChangeDetectorRef} from '@angular/core';
import {EmotionService} from '../services/emotion.service';
import {MatTableDataSource} from '@angular/material/table';
import {MatPaginator} from '@angular/material/paginator';
import {Router} from '@angular/router';
import {EmotionStateService} from '../services/emotion-state.service';
import {MatSort} from "@angular/material/sort";
import {EmotionRecord} from "../models/emotion.model";
import {MatSnackBar, MatSnackBarRef, SimpleSnackBar} from "@angular/material/snack-bar";
import {Observable} from "rxjs";


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

    snackBarRef: MatSnackBarRef<SimpleSnackBar> | null = null;

    @ViewChild(MatPaginator, {static: false}) paginator!: MatPaginator;

    @ViewChild(MatSort) sort!: MatSort;

    isLoading: boolean = true;

    snackBarDuration = 10000;
    private record: EmotionRecord | undefined;

    constructor(
        private emotionService: EmotionService,
        private router: Router,
        private emotionStateService: EmotionStateService,
        private changeDetector: ChangeDetectorRef,
        private snackBar: MatSnackBar
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

    refresh(): Observable<EmotionRecord[]> {
        const refresh = this.emotionService.fetchEmotionRecordsForCurrentUser()
        refresh.subscribe((data) => {
            this.dataSource.data = data;
            this.changeDetector.detectChanges();
        });
        return refresh;
    }

    onRowClicked(record: any): void {
        this.emotionStateService.updateNewEmotion(record); // Save the selected emotion record in the state
        this.router.navigate(['/display-emotion']);
    }

    getDateForSorting(data: any): number {
        return new Date(data.created).getTime();
    }

    onDelete(record: EmotionRecord): void {
      this.snackBar.open(`Deleting emotion record: ${record.notes[0]?.title}`, 'Close', {
        duration: this.snackBarDuration,
      })
        record.isDeleted = true;
        this.emotionService.updateEmotionRecord(record).subscribe({
                next: record => {
                    this.refresh().subscribe(
                        () => {
                          this.snackBarRef = this.snackBar.open(`Emotion record deleted: ${record.notes[0]?.title}`, 'Undo', {
                            duration: this.snackBarDuration,
                          });
                          this.snackBarRef.onAction().subscribe(() => {
                            record.isDeleted = false;
                            this.emotionService.updateEmotionRecord(record).subscribe({
                              next: record => {
                                this.refresh().subscribe(
                                    () => {
                                        this.snackBar.open(`Emotion record restored: ${record.notes[0]?.title}`, 'Close', {
                                        duration: this.snackBarDuration,
                                        });
                                    }
                                )
                              },
                              error: err => this.handleError(err, `Failed to undo delete for emotion record: ${record.notes[0]?.title}`)
                            });
                          });
                          this.snackBarRef.afterDismissed().subscribe(info => {
                            if (!info.dismissedByAction) {
                              this.emotionService.deleteEmotionRecord(record).subscribe({
                                next: record => {
                                  this.refresh();
                                },
                                error: err => this.handleError(err, `Failed to delete emotion record: ${record.notes[0].title}`)
                              });
                            }
                          });
                        }
                    );
                },
                error: err => this.handleError(err, `Failed to delete emotion record: ${record.notes[0].title}`)
            }
        );
    }





    private handleError(err: any, s: string) {
        console.log(s);
        this.snackBar.open(s, 'Close', {
            duration: this.snackBarDuration,
        });
    }
}
