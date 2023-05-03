import {Component} from '@angular/core';
import {EmotionService} from "../services/emotion.service";
import {EmotionStateService} from "../services/emotion-state.service";
import {Note, SuggestedAction} from '../models/emotion.model';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";

@Component({
  selector: 'app-display-emotion',
  templateUrl: './display-emotion.component.html',
  styleUrls: ['./display-emotion.component.css']
})
export class DisplayEmotionComponent {


  constructor(private fb: FormBuilder, private emotionService: EmotionService, private emotionStateService: EmotionStateService) {
    this.noteForm = this.fb.group({
      note: ['', Validators.required]
    });
  }
  noteSaved: boolean = false;


  isLoading: boolean = true;


  noteForm: FormGroup;

  emotion: any = {}

  note: string = '';
  suggestedActions: SuggestedAction[] | null = null;

// Add this method to get suggested actions
  getSuggestedActions(): void {
    // Replace the following with the actual API call to get suggested actions
    this.suggestedActions = [
      {
        id: "engage_in_physical_activity",
        name: "Engage in physical activity",
        created: "2023-04-15T15:16:52"
      }
    ];
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
        },
        error: (error) => {
          console.error('Error inserting note', error);
        }
      });
    }
  }
}
