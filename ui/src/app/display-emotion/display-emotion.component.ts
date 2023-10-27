import {Component} from '@angular/core';
import {EmotionService} from "../services/emotion.service";
import {EmotionStateService} from "../services/emotion-state.service";
import { EmotionRecord, NoteTemplate, SuggestedAction, Tag} from '../models/emotion.model';
import {FormBuilder} from "@angular/forms";
import {MatSnackBar} from "@angular/material/snack-bar";
import {NoteService} from '../services/note.service';
import {DateService} from "../services/date.service";
import {MatChipInputEvent} from "@angular/material/chips";


@Component({
  selector: 'app-display-emotion',
  templateUrl: './display-emotion.component.html',
  styleUrls: ['./display-emotion.component.css']
})
export class DisplayEmotionComponent {


  constructor(private fb: FormBuilder, private emotionService: EmotionService,
              private emotionStateService: EmotionStateService,
              private snackBar: MatSnackBar,
              private noteService: NoteService,
              public dateService: DateService) {
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

  async getSuggestedActions(): Promise<void> {
    this.isLoadingActions = true;
    if (this.emotion != null && this.emotion.id != null) {
      this.emotionService.fetchSuggestedActionsForEmotionRecord(this.emotion.id).subscribe({
        next: (response) => {
          this.suggestedActions = response;
          this.isLoadingActions = false;
          console.log('Suggested actions received:', response);
        },
        error: (error) => {
          this.isLoadingActions = false;
          console.error('Error getting suggested actions', error);
          this.snackBar.open('Error getting suggested actions', 'Close', {
            duration: 5000,
          });
        }}
      )
    }
  }

  ngOnInit(): void {
    this.emotionStateService.newEmotionRecord$.subscribe((newEmotion) => {
      if (newEmotion) {
        this.emotion = newEmotion;
        this.isLoading = false;
        console.log('New emotion received:', newEmotion);
      }
    });

    this.noteService.getNoteTemplates().subscribe((noteTemplates) => {
      this.noteTemplates = noteTemplates;
      console.log('note templates received');
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
