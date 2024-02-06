import {
  AfterViewInit, Component, OnDestroy,
  OnInit,
  QueryList,
  ViewChildren
} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {EmotionService} from '../services/emotion.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {EmotionCacheService} from '../services/emotion-cache.service';

import {
    Emotion,
    EmotionData, EmotionDetectionResult, EmotionFromNoteResult,
    EmotionRecord,
    EmotionTypesWithEmotions,
    EmotionWithSubEmotions, Note,
    SubEmotion,
    SubEmotionWrapper, Tag,
    Trigger
} from "../models/emotion.model";
import {AuthService} from "../services/auth.service";
import {from, Subscription} from "rxjs";
import {EmotionStateService} from "../services/emotion-state.service";
import {Router} from "@angular/router";
import {DateService} from "../services/date.service";
import {MatOption, ThemePalette} from "@angular/material/core";
import {NoteService} from "../services/note.service";


@Component({
  selector: 'app-create-emotion',
  templateUrl: './create-emotion.component.html',
  styleUrls: ['./create-emotion.component.css'],
  providers: []
})
export class CreateEmotionComponent implements OnInit, AfterViewInit, OnDestroy {
  isLoadingEmotionCache: boolean = true;

  emotionForm: FormGroup;
  sliderColor = 'rgba(75, 192, 192, 0.2)';

  emotionCache: EmotionData | undefined;

  emotionTypesWithEmotions: EmotionTypesWithEmotions[] | undefined;
  emotionWithSubEmotions: EmotionWithSubEmotions[] | undefined;

  noteText: string | null = null;

  step = 0;

  maxNoteLength = 2000; // TODO: Should be fetched from the backend

  isDetectingEmotionWithAI: boolean = false;
  isSavingEmotionRecord: boolean = false;

  emotionTypes: string[] = [];


  @ViewChildren('emotionTypeOptions') emotionTypeOptions!: QueryList<MatOption>;
  @ViewChildren('emotionOptions') emotionOptions!: QueryList<MatOption>;
  @ViewChildren('subEmotionOptions') subEmotionOptions!: QueryList<MatOption>;
  @ViewChildren('triggerOptions') triggerOptions!: QueryList<MatOption>;

  private emotionTypesSubscription!: Subscription;
  private emotionSelectSubscription!: Subscription;
  private subEmotionSelectSubscription!: Subscription;
  private triggerSubscription!: Subscription;
  private emotionDetected: EmotionDetectionResult | undefined;

  placeHolderText: string = "Try to describe how this emotion is affecting your daily activities or your interactions with others. Include more context or personal thoughts to convey your emotions more clearly. Are there any noticeable patterns or recurring events? How do you wish to feel instead? What steps do you think you could take to influence your emotional state? Remember, you can also use #hashtags to categorize or highlight key points in your note. To add a todo, simply enclose it in double square brackets like this: [[<your todo here>]]. ";

  constructor(private fb: FormBuilder, private emotionService: EmotionService,
              private authService: AuthService,
              private emotionStateService: EmotionStateService,
              private router: Router,
              private snackBar: MatSnackBar,
              private emotionCacheService: EmotionCacheService,
              private noteService: NoteService,
              private dateService: DateService) {
    this.emotionForm = this.fb.group({
      emotionType: [''],
      intensity: [0],
      emotion: [''],
      trigger: [''],
      subEmotion: [''],
      emotionDate: [new Date()],
      isPublic: [false],
      emotionNote: [''],
      tags: [[]],
      todos: [[]],
      textTitle: [''],
      description: [''],
      suggestion: [''],
      emotionTime: [''],
    });
  }

  ngOnDestroy(): void {
    // Unsubscribe from all Observables
    if (this.emotionTypesSubscription) {
      this.emotionTypesSubscription.unsubscribe();
    }
    if (this.emotionSelectSubscription) {
      this.emotionSelectSubscription.unsubscribe();
    }
    if (this.subEmotionSelectSubscription) {
      this.subEmotionSelectSubscription.unsubscribe();
    }
    if (this.triggerSubscription) {
      this.triggerSubscription.unsubscribe();
    }
  }

  ngAfterViewInit() {

  }

  ngOnInit(): void {
    this.emotionCacheService.emotionCache$.subscribe((cachedEmotionData) => {
      if (cachedEmotionData) {
        this.emotionCache = cachedEmotionData;
        this.isLoadingEmotionCache = false;
      } else {
          this.updateEmotionCache();
      }
    });
    this.emotionForm.controls['intensity'].setValue(1);
  }

    private updateEmotionCache() {
        this.emotionService.getEmotionCache().subscribe({
            next: (emotionCache) => {
                this.emotionCache = emotionCache;
                this.emotionCacheService.updateEmotionCache(emotionCache);
            },
            error: (error) => {
                console.error('Error fetching emotion cache:', error);
                this.isLoadingEmotionCache = false;
                this.snackBar.open('Failed to fetch emotion cache', 'Close', {});
            },
            complete: () => {
                console.log('Emotion cache fetch completed');
                this.isLoadingEmotionCache = false;
            }
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
    const subEmotions: any[] = [];
    if (emotionFromData.subEmotion?.subEmotionId) {
      subEmotions.push({subEmotionId: emotionFromData.subEmotion.subEmotionId});
    }
    const triggers: any[] = [];
    if (emotionFromData.trigger?.triggerId) {
      triggers.push({triggerId: emotionFromData.trigger.triggerId});
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

    const tags: Tag[] = [];
    if (emotionFromData.tags) {
      emotionFromData.tags.forEach((tag: Tag) => {
        tags.push(tag);
      });
    }

    return {
      userId: decodedToken.userId,
      emotionType: emotionFromData.emotionType ?? "Unknown",
      intensity: emotionFromData.intensity,
      emotion: emotion as Emotion,
      subEmotions: subEmotions as SubEmotion[],
      triggers: triggers as Trigger[],
      notes: notes as Note[],
      tags: tags as Tag[],
      created: this.dateService.formatDateToIsoString(emotionFromData.emotionDate)
    };
  }
}
