import {Component} from '@angular/core';
import {EmotionService} from "../services/emotion.service";
import {EmotionStateService} from "../services/emotion-state.service";
import { SuggestedAction } from '../models/emotion.model';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";

@Component({
  selector: 'app-display-emotion',
  templateUrl: './display-emotion.component.html',
  styleUrls: ['./display-emotion.component.css']
})
export class DisplayEmotionComponent {


  constructor(private fb: FormBuilder,private emotionService: EmotionService, private emotionStateService: EmotionStateService) {
    this.noteForm = this.fb.group({
      note: ['', Validators.required]
    });
  }
  noteForm: FormGroup;

  emotion: any = {
    id: 17,
    userId: 4,
    emotionId: "Interest",
    intensity: 4,
    subEmotions: [
      {
        subEmotionId: "Engagement",
        subEmotionName: "Engagement",
        parentEmotionId: "Interest"
      }
    ],
    triggers: [
      {
        triggerId: 1,
        triggerName: "People",
        description: "People",
        created: "2023-03-31T07:13:18"
      }
    ],
    created: "2023-04-29T22:17:58"
  };

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
        // Add the new emotion to the list of displayed emotions, or update the list of displayed emotions
        console.log('New emotion received:', newEmotion);
      }
    });
  }

  async onSubmitNote(): Promise<void> {
    if (this.noteForm.valid) {
      const note = this.noteForm.value.note;
      // ...submit the note to the backend, and associate it with the emotion record...
    }
  }
}
