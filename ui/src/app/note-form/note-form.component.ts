import {Component, Input} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {EmotionService} from "../services/emotion.service";
import {AuthService} from "../services/auth.service";
import {EmotionStateService} from "../services/emotion-state.service";
import {Router} from "@angular/router";
import {MatSnackBar} from "@angular/material/snack-bar";
import {DateService} from "../services/date.service";
import {from} from "rxjs";
import {Emotion, EmotionRecord, Note, SubEmotion, Tag, Trigger} from "../models/emotion.model";



@Component({
  selector: 'app-note-form',
  templateUrl: './note-form.component.html',
  styleUrls: ['./note-form.component.css']
})
export class NoteFormComponent {
  @Input() emotionForm: FormGroup;
  isSavingEmotionRecord: boolean = false;
  maxNoteLength = 2000; // TODO: Should be fetched from the backend
  placeHolderText: string = "Try to describe how this emotion is affecting your daily activities or your interactions with others. Include more context or personal thoughts to convey your emotions more clearly. Are there any noticeable patterns or recurring events? How do you wish to feel instead? What steps do you think you could take to influence your emotional state? Remember, you can also use #hashtags to categorize or highlight key points in your note. To add a todo, simply enclose it in double square brackets like this: [[<your todo here>]]. ";
  computeNoteLength(): number {
    return this.emotionForm.get('emotionNote')?.value?.length ?? 0;
  }


  isRecording = false;
  isTranscribing = false;

  constructor(private fb: FormBuilder, private emotionService: EmotionService,
              private authService: AuthService,
              private router: Router,
              private snackBar: MatSnackBar,
              private emotionStateService: EmotionStateService,
              private dateService: DateService) {
    this.emotionForm = this.fb.group({
      emotionDate: [new Date()],
      emotionNote: [''],
      textTitle: [''],
      emotionTime: [''],
    });
  }

  async onSubmit(): Promise<void> {
    console.log('Submitting emotion record: ', this.isSavingEmotionRecord);
    if (this.emotionForm.valid) {
      const emotionFromData = this.emotionForm.value;
      const emotionRecord = this.convertEmotionFromDataToEmotionRecord(emotionFromData);
      console.log(`Emotion record to be inserted: ${JSON.stringify(emotionRecord)}`);
      try {
        this.isSavingEmotionRecord = true;
        from(this.emotionService.insertEmotionRecord(emotionRecord)).subscribe(
          {
            next: (response) => {
              console.log('Emotion record inserted successfully', response);
              this.emotionStateService.updateNewEmotion(response);
              this.isSavingEmotionRecord = false;
              this.router.navigate(['/display-emotion']);
            },
            error: (error) => {
              console.error('Error inserting emotion record', error);
              this.isSavingEmotionRecord = false;
              this.snackBar.open('Failed to submit the emotion record', 'Close', {
                duration: 5000,
                panelClass: ['error-snackbar']
              });
            }
          }
        )
      } catch (error) {
        console.error(error);
      }
    }
  }

  convertEmotionFromDataToEmotionRecord(emotionFromData: any): EmotionRecord {
    const decodedToken = this.authService.fetchDecodedToken();
    let emotion: any = null;
    if (emotionFromData.emotion?.emotion?.id) {
      emotion = {};
      emotion.id = emotionFromData.emotion.emotion.id;
    }

    const notes: any[] = [];
    if (emotionFromData.emotionNote) {
      const note: Note = {
        text: emotionFromData.emotionNote,
        title: emotionFromData.textTitle,
        description: emotionFromData.description,
        suggestion: emotionFromData.suggestion,
        todos: emotionFromData.todos
      }
      notes.push(note);
    }

    return {
      userId: decodedToken.userId,
      emotionType: "Unknown",
      intensity: 0,
      emotion: emotion as Emotion,
      subEmotions: [] as SubEmotion[],
      triggers: [] as Trigger[],
      notes: notes as Note[],
      tags: [] as Tag[],
      created: this.dateService.formatDateToIsoString(emotionFromData.emotionDate)
    };
  }

  onTranscriptionReady(transcription: string) {
    const currentNote = this.emotionForm.get('emotionNote')?.value || '';
    this.emotionForm.get('emotionNote')?.setValue(`${currentNote} ${transcription}`);
  }
}
