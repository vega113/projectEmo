import {Component} from '@angular/core';
import {EmotionService} from "../services/emotion.service";
import {EmotionStateService} from "../services/emotion-state.service";
import { EmotionRecord, NoteTemplate, SuggestedAction, Tag} from '../models/emotion.model';
import {MatSnackBar} from "@angular/material/snack-bar";
import {DateService} from "../services/date.service";
import {MatChipInputEvent} from "@angular/material/chips";
import { Router } from '@angular/router';
import {MatDialog} from "@angular/material/dialog";
import {EmotionAnalyzerComponent} from "../emotion-analyzer/emotion-analyzer.component";
import {formatDate} from "@angular/common";


@Component({
  selector: 'app-display-emotion',
  templateUrl: './display-emotion.component.html',
  styleUrls: ['./display-emotion.component.css']
})
export class DisplayEmotionComponent {


  constructor(private emotionService: EmotionService,
              private emotionStateService: EmotionStateService,
              private snackBar: MatSnackBar,
              public dateService: DateService,
              private router: Router,
              public dialog: MatDialog) {
  }

  noteTemplates: NoteTemplate[] | null = null;


  isLoadingNotes: boolean = false;
  isLoadingActions: boolean = false;


  isLoading: boolean = true;


  emotion: EmotionRecord | null = null;

  note: string = '';

  suggestedActions: SuggestedAction[] | null = null;

  separatorKeysCodes: number[] = [13, 188];
  addOnBlur: any = true;
  openAnalyzeManuallyDialog() {
    console.log('Open analyze manually dialog', this.emotion );
    const dialogRef = this.dialog.open(EmotionAnalyzerComponent, {
      width: '15%', height: '55%', data: { emotionRecord: this.emotion }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        alert(`The dialog was closed with result: ${result}`);
        dialogRef.close();
      }
    });
  }

  ngOnInit(): void {
    this.isLoading = true;
    this.emotionStateService.newEmotionRecord$
        .subscribe((newEmotion) => {
          if (newEmotion) {
            this.emotion = newEmotion;
            this.isLoading = false;
            console.log('New emotion received:', newEmotion);
          } else {
            this.router.navigate(['/emotions-timeline']).
            then(() => console.log('Navigated to emotions-timeline page'));
            this.isLoading = false;
          }
        });
  }


  deleteTag(tag: Tag) {
    console.log('remove tag', tag);
    this.emotionService.deleteTag(tag.tagId!)
      .subscribe({
        next: (isDeleted) => {
          if (isDeleted) {
            if (this.emotion?.tags) {
              this.emotion.tags = this.emotion.tags?.filter((t: Tag) => {
                return t.tagId !== tag.tagId;
              });
              this.snackBar.open(`Tag deleted successfully: ${tag.tagName}`, 'Close', {
                duration: 5000,
              });
              console.log('Tag deleted successfully');
              this.emotionStateService.updateNewEmotion(this.emotion);
            }
          } else {
            this.snackBar.open(`Error deleting tag ${tag.tagName}`, 'Close', {
              duration: 5000,
            });
          }
        },
        error: (error) => {
          console.error('Error deleting tag', error);
          this.isLoadingNotes = false;
          this.snackBar.open('Error deleting tag', 'Close', {
            duration: 5000,
          });
        }
      });
  }
  addTag(event: MatChipInputEvent) {
    const tagName = (event.value || '').trim();
    console.log('Add tag', tagName);
    this.emotionService.addTag(tagName!, this.emotion!.id!)
      .subscribe({
        next: (isAdded) => {
          if (isAdded) {
            this.emotion?.tags?.push({tagName: tagName});
          }
          event.chipInput!.clear();
          this.snackBar.open(`Tag added successfully: ${tagName}`, 'Close', {
            duration: 5000,
          });
        },
        error: (error) => {
          console.error('Error adding tag', error);
          this.isLoadingNotes = false;
          this.snackBar.open(`Error adding tag ${tagName}`, 'Close', {
            duration: 5000,
          });
        }
      });
  }

  protected readonly formatDate = formatDate;

  formatDateFromDb() {
    return this.dateService.formatDateFromDb( this.emotion?.created!)
  }
}
