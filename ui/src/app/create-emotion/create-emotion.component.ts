import {
  AfterViewInit, Component, OnDestroy,
  OnInit,
  QueryList,
  ViewChildren
} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
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
    SubEmotionWithActions, Tag,
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

  maxNoteLength = 500;

  isLoadingNotes: boolean = false;
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

  placeHolderText: string = "Try to describe how this emotion is affecting your daily activities or your interactions with others. Are there any noticeable patterns or recurring events? How do you wish to feel instead? What steps do you think you could take to influence your emotional state? Remember, you can also use #hashtags to categorize or highlight key points in your note.";

  constructor(private fb: FormBuilder, private emotionService: EmotionService,
              private authService: AuthService,
              private emotionStateService: EmotionStateService,
              private router: Router, private snackBar: MatSnackBar,
              private emotionCacheService: EmotionCacheService,
              private noteService: NoteService,
              private dateService: DateService) {
    this.emotionForm = this.fb.group({
      emotionType: ['', Validators.required],
      intensity: [''],
      emotion: [''],
      trigger: [''],
      subEmotion: [''],
      emotionDate: [new Date()],
      isPublic: [false],
      emotionNote: [''],
      tags: [[]],
      description: [''],
      suggestion: [''],
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



  setStep(index: number) {
    this.step = index;
  }

  detectEmotions() {
    this.isLoadingNotes = true;
    console.log('Detecting emotion for text: ', this.emotionForm.get("emotionNote")?.value);
    this.noteService.detectEmotion(this.emotionForm.get("emotionNote")?.value).subscribe({
      next: (response) => {
        console.log('Emotion detected successfully', response);
        this.handleNoteSubmission(response);
        this.isLoadingNotes = false;
        this.setStep(1);
      },
      error: (error) => {
        console.error('Error detecting emotion', error);
        this.isLoadingNotes = false;
        this.snackBar.open('Error detecting emotion', 'Close', {
          duration: 5000,
        });
      }
    });
  }

  skipToManualEntry() {
    this.setStep(1); // Move to manual entry without detecting
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
        description: emotionFromData.description,
        suggestion: emotionFromData.suggestion
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
      emotionType: emotionFromData.emotionType,
      intensity: emotionFromData.intensity,
      emotion: emotion as Emotion,
      subEmotions: subEmotions as SubEmotion[],
      triggers: triggers as Trigger[],
      notes: notes as Note[],
      tags: tags as Tag[],
      created: this.dateService.formatDateToIsoString(emotionFromData.emotionDate)
    };
  }

  makeEmotionTypesList(): string[] {
    if (this.emotionCache && this.emotionCache.emotionTypes) {
      this.emotionTypesWithEmotions = this.emotionCache.emotionTypes;
      this.emotionTypes = this.emotionCache.emotionTypes.map(emotionTypeObject => emotionTypeObject.emotionType);
      return this.emotionTypes
    } else {
      return [];
    }
  }

  makeEmotionsList(): EmotionWithSubEmotions[] {
    if (this.emotionCache && this.emotionTypesWithEmotions) {
      const selectedEmotionType = this.emotionForm.get('emotionType')?.value;
      const emotionTypeObject = this.emotionTypesWithEmotions.find(emotionTypeObject => emotionTypeObject.emotionType === selectedEmotionType);
      if (emotionTypeObject) {
        this.emotionWithSubEmotions = emotionTypeObject.emotions;
        return emotionTypeObject.emotions;
      } else {
        return [];
      }
    } else {
      return [];
    }
  }

  makeSubEmotionsList(): SubEmotionWithActions[] {
    if (this.emotionCache) {
      const selectedEmotionObject = this.emotionForm.get('emotion')?.value as EmotionWithSubEmotions;
      if (selectedEmotionObject) {
        return selectedEmotionObject.subEmotions;
      } else {
        return [];
      }
    } else {
      return [];
    }
  }

  makeTriggersList(): Trigger[] {
    if (this.emotionCache) {
      return this.emotionCache.triggers;
    } else {
      return [];
    }
  }


  handleNoteSubmission(emotionFromResult: EmotionFromNoteResult) {

    this.emotionDetected = emotionFromResult.emotionDetection;
    this.noteText = emotionFromResult.note.text;

    if(this.emotionDetected == null) {
      this.emotionForm.get('createFromNote')?.setValue(false);
    } else {
      console.log('Emotion detected from note');
      this.emotionForm.get('createFromNote')?.setValue(false);
      this.emotionForm.get('note')?.setValue(this.noteText);
      this.emotionForm.get('suggestion')?.setValue(this.emotionDetected.suggestion);
      this.emotionForm.get('description')?.setValue(this.emotionDetected.description);

      this.emotionForm.controls['emotionNote'].setValue(this.noteText);

      this.emotionForm.controls['emotionType'].setValue(this.emotionDetected.emotionType);
      this.emotionForm.controls['intensity'].setValue(this.emotionDetected.intensity);

      this.emotionOptions.find(option =>
        option.value.emotion.id === this.emotionDetected?.mainEmotionId)?.select();


      this.subEmotionSelectSubscription = this.subEmotionOptions.changes.subscribe((options: QueryList<MatOption>) => {
        console.log("subEmotionOptions changed", options);
        const subEmotionId = this.emotionDetected?.subEmotionId;

        const optionToSelect = options.find(option =>
          option.value.subEmotionName === subEmotionId);


        if(optionToSelect) {
          Promise.resolve().then(() => optionToSelect.select());
        } else {
          const subEmotionsFromOptions = options.map(option => option.value.subEmotionName).join(', ');
          console.warn(`Associated emotion ${subEmotionId} not found in the list of sub emotions: ${subEmotionsFromOptions}.`);
        }
        this.subEmotionSelectSubscription.unsubscribe();
      });

      if (this.emotionDetected?.triggers != null && this.emotionDetected?.triggers.length > 0) {
        this.triggerOptions.find(option =>
          option.value.triggerName === this.emotionDetected?.triggers[0]?.triggerName)?.select();
      }

      if(this.emotionDetected?.tags != null && this.emotionDetected?.tags.length > 0) {
        this.emotionForm.controls['tags'].setValue(this.emotionDetected?.tags);
      }

      this.snackBar.open(this.emotionDetected?.description + "\n" + this.emotionDetected?.suggestion, 'Close', {
        duration: 40000,
        panelClass: ['emotion-snackbar']
      });
    }
  }

  getSliderColor(emotionType: string): ThemePalette {
    switch (emotionType) {
      case 'Positive':
        return 'primary';
      case 'Neutral':
        return 'accent';
      case 'Negative':
        return 'warn';
      default:
        return undefined;
    }
  }

}
