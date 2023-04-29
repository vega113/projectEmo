import {Component} from '@angular/core';
import {EmotionService} from "../services/emotion.service";
import {EmotionRecord} from "../models/emotion.model";
import {EmotionStateService} from "../services/emotion-state.service";

@Component({
  selector: 'app-display-emotion',
  templateUrl: './display-emotion.component.html',
  styleUrls: ['./display-emotion.component.css']
})
export class DisplayEmotionComponent {
  constructor(private emotionService: EmotionService, private emotionStateService: EmotionStateService) {
  }

  ngOnInit(): void {
    this.emotionStateService.newEmotionRecord$.subscribe((newEmotion) => {
      if (newEmotion) {
        // Add the new emotion to the list of displayed emotions, or update the list of displayed emotions
        console.log('New emotion received:', newEmotion);
      }
    });
  }
}
