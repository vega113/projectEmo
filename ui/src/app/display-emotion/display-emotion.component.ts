import {Component} from '@angular/core';
import {EmotionService} from "../services/emotion.service";
import {EmotionStateService} from "../services/emotion-state.service";
import { EmotionRecord, NoteTemplate, SuggestedAction, Tag} from '../models/emotion.model';
import {MatSnackBar} from "@angular/material/snack-bar";
import {DateService} from "../services/date.service";
import {MatChipInputEvent} from "@angular/material/chips";
import { Router } from '@angular/router';


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
              private router: Router) {
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
              this.emotionStateService.updateNewEmotion(this.emotion);
              // this.noteForm.reset();
            }
          }
          console.log('Tag deleted successfully');
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
          console.log('Tag added successfully');
        },
        error: (error) => {
          console.error('Error adding tag', error);
          this.isLoadingNotes = false;
          this.snackBar.open('Error adding tag', 'Close', {
            duration: 5000,
          });
        }
      });
  }
}
