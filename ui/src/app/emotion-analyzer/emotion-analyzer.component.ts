import {
  AfterViewChecked,
  AfterViewInit,
  Component,
  Inject,
  OnChanges,
  OnInit,
  QueryList,
  SimpleChanges,
  ViewChildren
} from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { EmotionService } from '../services/emotion.service';
import {
  EmotionData,
  EmotionDetectionResult, EmotionFromNoteResult, EmotionRecord, EmotionTypesWithEmotions,
  EmotionWithSubEmotions,
  SubEmotionWrapper,
  Trigger
} from "../models/emotion.model";
import {MatOption, ThemePalette} from "@angular/material/core";
import {from, Subscription} from "rxjs";
import {EmotionStateService} from "../services/emotion-state.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {EmotionCacheService} from "../services/emotion-cache.service";
import {NoteService} from "../services/note.service";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";

@Component({
  selector: 'app-emotion-analyzer',
  templateUrl: './emotion-analyzer.component.html',
  styleUrls: ['./emotion-analyzer.component.css']
})
export class EmotionAnalyzerComponent implements OnInit {

  emotionRecord: EmotionRecord | undefined;
  emotionForm: FormGroup;

  emotionTypes: string[] = [];


  @ViewChildren('emotionTypeOptions') emotionTypeOptions!: QueryList<MatOption>;
  @ViewChildren('emotionOptions') emotionOptions!: QueryList<MatOption>;
  @ViewChildren('subEmotionOptions') subEmotionOptions!: QueryList<MatOption>;
  @ViewChildren('triggerOptions') triggerOptions!: QueryList<MatOption>;

  private subEmotionSelectSubscription!: Subscription;
  private emotionDetected: EmotionDetectionResult | undefined;

  isLoadingEmotionCache: boolean = true;

  emotionCache: EmotionData | undefined;

  emotionTypesWithEmotions: EmotionTypesWithEmotions[] | undefined;
  emotionWithSubEmotions: EmotionWithSubEmotions[] | undefined;

  isDetectingEmotionWithAI: boolean = false;
  isSavingEmotionRecord: boolean = false;


  constructor(private fb: FormBuilder, private emotionService: EmotionService,
              private emotionStateService: EmotionStateService,
              private snackBar: MatSnackBar,
              private emotionCacheService: EmotionCacheService,
              private noteService: NoteService,
              public dialogRef: MatDialogRef<EmotionAnalyzerComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any
              ) {
    this.emotionForm = this.fb.group({
      emotionType: [''],
      intensity: [0],
      emotion: [''],
      trigger: [''],
      subEmotion: [''],
      emotionDate: [''],
      emotionNote: [''],
      tags: [[]],
      todos: [[]],
      textTitle: [''],
      description: [''],
      suggestion: [''],
      emotionTime: [''],
      createFromNote: [false],
    });
    this.emotionRecord = data.emotionRecord;
  }

  refreshEmotionCache() {
    this.makeEmotionTypesList();
    this.makeEmotionList();
    this.makeSubEmotionsList();
    this.makeTriggersList();
  }

  ngOnInit(): void {
    this.emotionCacheService.emotionCache$.subscribe((cachedEmotionData) => {
      if (cachedEmotionData) {
        this.emotionCache = cachedEmotionData;
        this.isLoadingEmotionCache = false;
        console.log('Emotion cache in EmotionAnalyzerComponent: ', this.emotionCache);
        this.copyFromInputEmotionRecordToForm();
      } else {
        console.log('Emotion cache not found in EmotionAnalyzerComponent');
        this.updateEmotionCache();
      }
      console.log('Emotion record in EmotionAnalyzerComponent: ', this.emotionRecord);
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
        this.copyFromInputEmotionRecordToForm()
        console.log('Emotion cache fetch completed');
        this.isLoadingEmotionCache = false;

      }
    });
  }

  convertEmotionFromDataToEmotionRecord(emotionFromData: any, inputEmotionRecord: EmotionRecord): EmotionRecord {
    return {
      id: inputEmotionRecord.id,
      emotionType: emotionFromData.emotionType,
      userId: inputEmotionRecord.userId,
      emotion: {
        id: emotionFromData.emotion.emotion.id,
      },
      intensity: emotionFromData.intensity,
      subEmotions: [],
      subEmotionId: emotionFromData.subEmotion?.subEmotionId,
      triggers: [],
      triggerId: emotionFromData.trigger?.triggerId,
      notes: [],
      tags: emotionFromData.tags,
    }
  }

  async onSubmit(): Promise<void> {
    console.log('Submitting emotion record: ', this.isSavingEmotionRecord);
    if (this.emotionForm.valid) {
      const emotionFromData = this.emotionForm.value;
      const emotionRecord = this.convertEmotionFromDataToEmotionRecord(emotionFromData,
        this.emotionRecord!);
      console.log(`Emotion record to be updated: ${JSON.stringify(emotionRecord)}`);
      try {
        this.isSavingEmotionRecord = true;
        from(this.emotionService.updateEmotionRecord(emotionRecord)).subscribe(
          {
            next: (response) => {
              console.log('Emotion record updated successfully', response);
              this.emotionStateService.updateNewEmotion(response);
              this.isSavingEmotionRecord = false;
              this.isSavingEmotionRecord = false;
              this.dialogRef.close();
              this.snackBar.open('Updated the note with emotion details', 'Close', {
                duration: 5000,
                panelClass: ['error-snackbar']
              });
            },
            error: (error) => {
              console.error('Error updating emotion record', error);
              this.isSavingEmotionRecord = false;
              this.snackBar.open('Failed to update the emotion record', 'Close', {
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

  makeEmotionTypesList(): string[] {
    if (this.emotionCache && this.emotionCache.emotionTypes) {
      this.emotionTypesWithEmotions = this.emotionCache.emotionTypes;
      this.emotionTypes = this.emotionCache.emotionTypes.map(emotionTypeObject => emotionTypeObject.emotionType);
      return this.emotionTypes
    } else {
      return [];
    }
  }

  makeEmotionList(): EmotionWithSubEmotions[] {
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
      console.log('Emotion cache not found', this.emotionCache);
      return [];
    }
  }

  makeSubEmotionsList(): SubEmotionWrapper[] {
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
    console.log('Emotion detected from note', emotionFromResult);

    this.emotionDetected = emotionFromResult.emotionDetection;

    if(this.emotionDetected == null) {
      this.emotionForm.get('createFromNote')?.setValue(false);
    } else {
      console.log('Updating emotion from result');

      this.emotionForm.controls['emotionType'].setValue(this.emotionDetected.emotionType);
      this.emotionForm.controls['intensity'].setValue(this.emotionDetected.intensity);



      this.setFormControlValue(
        this.makeEmotionList(),
        'emotion',
        (emotion: EmotionWithSubEmotions) => emotion.emotion.id,
        this.emotionDetected?.mainEmotionId
      )

      // Promise.resolve().then(() =>  this.setFormControlValue(
      //   this.makeSubEmotionsList(),
      //   'subEmotion',
      //   (subEmotion: SubEmotionWrapper) => subEmotion.subEmotion.subEmotionId,
      //   this.emotionDetected?.subEmotionId
      // ));



      this.emotionTypeOptions?.changes?.subscribe((options: QueryList<MatOption>) => {
        console.log("emotionTypeOptions changed", options);
        this.emotionOptions.find(option =>
        {
          console.log('option', option);
          return option.value.emotion.id === this.emotionDetected?.mainEmotionId;
        })?.select();

      });



      this.subEmotionSelectSubscription = this.subEmotionOptions?.changes.subscribe((options: QueryList<MatOption>) => {
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


      this.setFormControlValue(
        this.makeTriggersList(),
        'trigger',
        (trigger: Trigger) => trigger.triggerId,
        this.emotionDetected?.triggers[0]?.triggerId
      )
    }
  }


  private setFormControlValue<T>(
    items: T[],
    controlName: string,
    keyExtractor: (item: T) => any,
    keyToCompare: any
  ): void {
    if(items == null) {
      console.error(controlName + ", Items to select are null");
      return;
    }
    const itemToSelect = items.find(item => keyExtractor(item) === keyToCompare);
    if (itemToSelect == null) {
      console.error(controlName + ", Item not found for key", keyToCompare);
    } else {
      console.log('Item found', itemToSelect);
    }
    this.emotionForm.controls[controlName].setValue(itemToSelect);
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

  detectEmotions() {
    this.isDetectingEmotionWithAI = true;
    console.log('Detecting emotion for text: ', this.emotionForm.get("emotionNote")?.value);
    this.noteService.detectEmotion(this.emotionForm.get("emotionNote")?.value).subscribe({
      next: (response: EmotionFromNoteResult) => {
        console.log('Emotion detected successfully', response);
        this.handleNoteSubmission(response);
        this.isDetectingEmotionWithAI = false;
      },
      error: (error) => {
        console.error('Error detecting emotion', error);
        this.isDetectingEmotionWithAI = false;
        this.snackBar.open('Error detecting emotion', 'Close', {
          duration: 5000,
        });
      }
    });
  }


  ngOnDestroy(): void {
    if (this.subEmotionSelectSubscription) {
      this.subEmotionSelectSubscription.unsubscribe();
    }
  }

  private copyFromInputEmotionRecordToForm() {
    const existingDetectionResult: EmotionFromNoteResult = {
      emotionDetection: {
        emotionType: this.emotionRecord?.emotionType!,
        intensity: this.emotionRecord?.intensity!,
        mainEmotionId: this.emotionRecord?.emotion?.id,
        subEmotionId: this.emotionRecord?.subEmotions[0]?.subEmotionId,
        triggers: this.emotionRecord?.triggers!,
        tags: [],
        todos: [],
        textTitle: '',
        description: '',
        suggestion: '',
      },
      note: {
        text: '',
      }
    }
    this.refreshEmotionCache();
    // this.handleNoteSubmission(existingDetectionResult);
    this.setSelectOptionsForEditAccordingToExistingValues(existingDetectionResult);
  }

  onCancel(): void {
    this.emotionForm.reset();
    this.dialogRef.close();
  }

  private setSelectOptionsForEditAccordingToExistingValues(emotionFromResult: EmotionFromNoteResult) {
    // update the emotion type, intensity, emotion, sub emotion, trigger mat-select  html elements according
    // to the existing values, so we can edit them

    this.emotionCacheService.emotionCache$.subscribe((cachedEmotionData) => {
      this.setFormControlValue(
        this.emotionTypes,
        'emotionType',
        (emotionType: string) => emotionType,
        emotionFromResult.emotionDetection?.emotionType
      );
      this.setFormControlValue(
        this.makeEmotionList(),
        'emotion',
        (emotion: EmotionWithSubEmotions) => emotion.emotion.id,
        emotionFromResult.emotionDetection?.mainEmotionId
      );
      this.emotionForm.get('emotion')?.valueChanges.subscribe(() => {
        this.makeSubEmotionsList();
      });

      this.subEmotionOptions?.changes.subscribe(() => {
        this.setFormControlValue(
          this.makeSubEmotionsList(),
          'subEmotion',
          (subEmotion: SubEmotionWrapper) => subEmotion.subEmotion.subEmotionId,
          emotionFromResult.emotionDetection?.subEmotionId
        );
      });

    });
  }


}
