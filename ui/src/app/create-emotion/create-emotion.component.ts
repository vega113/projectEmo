import {
  AfterViewInit,
  Component,
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
import {MatOption} from "@angular/material/core";


@Component({
  selector: 'app-create-emotion',
  templateUrl: './create-emotion.component.html',
  styleUrls: ['./create-emotion.component.css'],
  providers: []
})
export class CreateEmotionComponent implements OnInit, AfterViewInit {
  isLoadingEmotionCache: boolean = true;

  emotionForm: FormGroup;
  emotionIntensityValue: number = 1;
  sliderColor = 'rgba(75, 192, 192, 0.2)';

  emotionCache: EmotionData | undefined;

  emotionTypesWithEmotions: EmotionTypesWithEmotions[] | undefined;
  emotionWithSubEmotions: EmotionWithSubEmotions[] | undefined;

  createFromNote = false;
  noteText: string | null = null;

  @ViewChildren('emotionOptions') emotionOptions!: QueryList<MatOption>;
  @ViewChildren('subEmotionOptions') subEmotionOptions!: QueryList<MatOption>;
  @ViewChildren('triggerOptions') triggerOptions!: QueryList<MatOption>;

  private emotionSelectSubscription!: Subscription;
  private subEmotionSelectSubscription!: Subscription;
  private triggerSubscription!: Subscription;
  private emotionDetected: EmotionDetectionResult | undefined;

  constructor(private fb: FormBuilder, private emotionService: EmotionService, private authService: AuthService,
              private emotionStateService: EmotionStateService, private router: Router, private snackBar: MatSnackBar,
              private emotionCacheService: EmotionCacheService,
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
      createFromNote: [false],
      tags: [[]]
    });
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

    this.emotionForm.get('createFromNote')?.valueChanges.subscribe(value => {
      console.log('createFromNote changed to:', value);
      // Other logic if needed
    });
    this.emotionForm.get('isPublic')?.valueChanges.subscribe(value => {
      console.log('Public changed to:', value);
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
    if (this.emotionForm.valid) {
      const emotionFromData = this.emotionForm.value;
      const emotionRecord = this.convertEmotionFromDataToEmotionRecord(emotionFromData);
      console.log(`Emotion record to be inserted: ${JSON.stringify(emotionRecord)}`);
      try {
        from(this.emotionService.insertEmotionRecord(emotionRecord)).subscribe(
          {
            next: (response) => {
              console.log('Emotion record inserted successfully', response);
              this.emotionStateService.updateNewEmotion(response);
              this.router.navigate(['/display-emotion']);
            },
            error: (error) => {
              console.error('Error inserting emotion record', error);
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
      notes.push({text: emotionFromData.emotionNote});
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
      intensity: this.emotionIntensityValue,
      emotion: emotion as Emotion,
      subEmotions: subEmotions as SubEmotion[],
      triggers: triggers as Trigger[],
      notes: notes as Note[],
      tags: tags as Tag[],
      created: this.dateService.formatDateToIsoString(emotionFromData.emotionDate)
    };
  }

  changeSliderColor(event: any) {
    // TODO: remove this method
    const intensity = (event.target as HTMLInputElement).valueAsNumber;
    const r = Math.round(255 * (intensity / 10));
    const g = Math.round(255 * (1 - intensity / 10));
    this.sliderColor = `rgb(${r}, ${g}, 0)`;
    if (intensity) {
      this.emotionIntensityValue = intensity;
    }
  }

  makeEmotionTypesList(): string[] {
    if (this.emotionCache && this.emotionCache.emotionTypes) {
      this.emotionTypesWithEmotions = this.emotionCache.emotionTypes;
      return this.emotionCache.emotionTypes.map(emotionTypeObject => emotionTypeObject.emotionType);
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

  handleNoteSubmission($event: EmotionFromNoteResult) {

    this.emotionDetected = $event.emotionDetection;
    this.noteText = $event.note.text;

    const findTriggerOptionToSelect = (triggerName: string | undefined) => {
      return this.triggerOptions.find(option =>
          option.value.triggerName === triggerName);
    }

    if(this.emotionDetected == null) {
      this.createFromNote = false;
      this.emotionForm.get('createFromNote')?.setValue(false);
    } else {
      console.log('Emotion detected from note');
      this.emotionForm.get('createFromNote')?.setValue(false);
      this.emotionForm.get('note')?.setValue(this.noteText);

      this.emotionForm.controls['emotionNote'].setValue(this.noteText);

      this.emotionForm.controls['emotionType'].setValue(this.emotionDetected.emotionType);
      this.emotionForm.controls['intensity'].setValue(this.emotionDetected.intensity);

      this.emotionForm.controls['emotionType'].valueChanges.subscribe((value) => {
        this.makeEmotionsList();
      });

      this.emotionSelectSubscription = this.emotionOptions.changes.subscribe((options) => {
        this.emotionOptions.find(option =>
          option.value.emotion.id === this.emotionDetected?.mainEmotionId)?.select();
        this.makeSubEmotionsList();
        this.emotionSelectSubscription.unsubscribe();
      });

      this.subEmotionSelectSubscription = this.subEmotionOptions.changes.subscribe((options: QueryList<MatOption>) => {
        console.log("subEmotionOptions changed", options);
        const subEmotionId = this.emotionDetected?.subEmotionId;

        const optionToSelect = options.find(option =>
            option.value.subEmotionName === subEmotionId);


        if(optionToSelect) {
          optionToSelect.select();
        } else {
          const subEmotionsFromOptions = options.map(option => option.value.subEmotionName).join(', ');
          console.warn(`Associated emotion ${subEmotionId} not found in the list of sub emotions: ${subEmotionsFromOptions}.`);
        }
        this.subEmotionSelectSubscription.unsubscribe();
      });

      this.triggerSubscription = this.triggerOptions.changes.subscribe((options) => {
        if(this.emotionDetected?.triggers != null && this.emotionDetected?.triggers.length > 0) {
          const triggerName = this.emotionDetected?.triggers[0].triggerName;
          findTriggerOptionToSelect.call(this, triggerName)?.select();
        }
        this.triggerSubscription.unsubscribe();
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
}
