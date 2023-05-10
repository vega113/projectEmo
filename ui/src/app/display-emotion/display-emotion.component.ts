import {Component} from '@angular/core';
import {EmotionService} from "../services/emotion.service";
import {EmotionStateService} from "../services/emotion-state.service";
import {Note, SuggestedAction} from '../models/emotion.model';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MatSnackBar} from "@angular/material/snack-bar";

@Component({
  selector: 'app-display-emotion',
  templateUrl: './display-emotion.component.html',
  styleUrls: ['./display-emotion.component.css']
})
export class DisplayEmotionComponent {


  constructor(private fb: FormBuilder, private emotionService: EmotionService,
              private emotionStateService: EmotionStateService,
              private snackBar: MatSnackBar) {
    this.noteForm = this.fb.group({
      note: ['', Validators.required]
    });
  }

  isLoadingNotes: boolean = false;
  isLoadingActions: boolean = false;

  noteSaved: boolean = false;


  isLoading: boolean = true;


  noteForm: FormGroup;

  emotion: any = {}

  note: string = '';

  suggestedActions: SuggestedAction[] | null = null;

// Add this method to get suggested actions
  async getSuggestedActions(): Promise<void> {
    this.isLoadingActions = true;
    // Replace the following with the actual API call to get suggested actions
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

  ngOnInit(): void {
    this.emotionStateService.newEmotionRecord$.subscribe((newEmotion) => {
      if (newEmotion) {
        this.emotion = newEmotion;
        this.isLoading = false;
        console.log('New emotion received:', newEmotion);
      }
    });
  }

  async onSubmitNote(): Promise<void> {
    this.isLoadingNotes = true;

    if (this.noteForm.valid) {
      const note = {
        text: this.noteForm.value.note,
      } as Note;
      this.emotionService.addNoteToEmotionRecord(this.emotion.id, note).subscribe({
        next: (response) => {
          this.emotionStateService.updateNewEmotion(response);
          this.noteForm.reset();
          console.log('Note inserted successfully', response);
          this.noteSaved = true;
          this.isLoadingNotes = false;
        },
        error: (error) => {
          console.error('Error inserting note', error);
          this.isLoadingNotes = false;
          this.snackBar.open('Error inserting note', 'Close', {
            duration: 5000,
          });
        }
      });
    }

  }
}
