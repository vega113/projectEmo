import {
  Component,
  Inject,
  OnInit,
  QueryList,
  ViewChildren
} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {EmotionService} from '../services/emotion.service';
import {
  EmotionData,
  EmotionFromNoteResult, EmotionRecord, EmotionTypesWithEmotions,
  EmotionWithSubEmotions,
  SubEmotionWrapper,
  Trigger
} from "../models/emotion.model";
import {MatOption, ThemePalette} from "@angular/material/core";
import {from, Observable, of, Subscription} from "rxjs";
import {EmotionStateService} from "../services/emotion-state.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {EmotionCacheService} from "../services/emotion-cache.service";
import {NoteService} from "../services/note.service";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {map, switchMap} from "rxjs/operators";

@Component({
  selector: 'app-emotion-analyzer',
  templateUrl: './emotion-analyzer.component.html',
  styleUrls: ['./emotion-analyzer.component.css']
})
export class EmotionAnalyzerComponent implements OnInit {

  emotionRecord: EmotionRecord | undefined;
  emotionForm: FormGroup;

  emotionTypes: string[] = [];

  private subscriptions: Subscription[] = []


  @ViewChildren('emotionTypeOptions') emotionTypeOptions!: QueryList<MatOption>;
  @ViewChildren('emotionOptions') emotionOptions!: QueryList<MatOption>;
  @ViewChildren('subEmotionOptions') subEmotionOptions!: QueryList<MatOption>;
  @ViewChildren('triggerOptions') triggerOptions!: QueryList<MatOption>;

  isLoadingEmotionCache: boolean = true;

  emotionCache$: Observable<EmotionData> | undefined;

  emotionTypesWithEmotions: EmotionTypesWithEmotions[] | undefined;
  emotionWithSubEmotions: EmotionWithSubEmotions[] | undefined;

  isDetectingEmotionWithAI: boolean = false;
  isSavingEmotionRecord: boolean = false;
  emotionTypes$: Observable<string[]>;
  mainEmotions$: Observable<EmotionWithSubEmotions[]> | undefined;
  subEmotions$: Observable<SubEmotionWrapper[]> | undefined;
  triggers$: Observable<Trigger[]> | undefined;


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

    this.emotionTypes$ = this.getEmotionTypes();
    this.mainEmotions$ = this.getMainEmotions();
    this.subEmotions$ = this.getSubEmotions();
    this.triggers$ = this.getTriggers();
  }

  ngOnInit(): void {
    const emotionCacheSubscription = this.emotionCacheService.emotionCache$.subscribe(
      (cachedEmotionData) => {
        if (cachedEmotionData) {
          this.emotionCache$ = of(cachedEmotionData);
          this.isLoadingEmotionCache = false;
          console.log('Emotion cache in EmotionAnalyzerComponent: ', cachedEmotionData);

        } else {
          console.log('Emotion cache not found in EmotionAnalyzerComponent');
          this.updateEmotionCache();
        }
        console.log('Emotion record in EmotionAnalyzerComponent: ', this.emotionRecord);
      });
    this.subscriptions.push(emotionCacheSubscription);

    const afterOpenedSubscription = this.dialogRef.afterOpened().subscribe(() => {
        this.populateEmotionAnalyzeDialog();
      }
    );
    this.subscriptions.push(afterOpenedSubscription);
  }

  private populateEmotionAnalyzeDialog() {
    this.setFormControlValueForObservable(
      this.emotionTypes$,
      'emotionType',
      (emotionType: string) => emotionType,
      this.emotionRecord?.emotionType
    );
    this.setFormControlValueForObservable(
      this.mainEmotions$,
      'emotion',
      (emotion: EmotionWithSubEmotions) => emotion.emotion.id,
      this.emotionRecord?.emotion?.id
    );
    this.copyFromInputEmotionRecordToForm();

    this.setFormControlValueForObservable(
      this.triggers$,
      'trigger',
      (trigger: Trigger) => trigger.triggerId,
      this.emotionRecord?.triggers[0]?.triggerId
    );

    this.emotionForm.controls['intensity'].setValue(this.emotionRecord?.intensity);
  }

  private updateEmotionCache() {
    const emotionCacheSubscription = this.emotionService.getEmotionCache().subscribe({
      next: (emotionCache) => {
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
    this.subscriptions.push(emotionCacheSubscription);
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
        const updateEmotionRecordSubscription = from(this.emotionService.updateEmotionRecord(emotionRecord)).
        subscribe(
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
        this.subscriptions.push(updateEmotionRecordSubscription);
      } catch (error) {
        console.error(error);
      }
    }
  }

  // TODO: Remove this method, detection is on display emotion
  handleNoteSubmission(emotionFromResult: EmotionFromNoteResult) {
    console.log('Emotion detected from note', emotionFromResult);
    this.emotionForm.get('createFromNote')?.setValue(false);
    this.emotionStateService.updateNewEmotion({
      id: this.emotionRecord?.id,
      emotionType: emotionFromResult.emotionDetection?.emotionType!,
      userId: this.emotionRecord?.userId,
      emotion: {
        id: emotionFromResult.emotionDetection?.mainEmotionId!,
      },
      intensity: emotionFromResult.emotionDetection?.intensity!,
      subEmotions: [],
      subEmotionId: emotionFromResult.emotionDetection?.subEmotionId!,
      triggers: [],
      triggerId: emotionFromResult.emotionDetection?.triggers[0]?.triggerId!,
      notes: emotionFromResult.note ? [emotionFromResult.note] : [],
      tags: emotionFromResult.emotionDetection?.tags!,
    } as EmotionRecord);
  }

  private setFormControlValueForObservable<T>(
    items: Observable<T[]> | undefined,
    controlName: string,
    keyExtractor: (item: T) => any,
    keyToCompare: any
  ): void {
    if (items == null) {
      console.error(controlName + ", Items to select are null");
      return;
    }
    items.subscribe((items) => {
      const itemToSelect = items.find(item => keyExtractor(item) === keyToCompare);
      if (itemToSelect == null) {
        console.error(controlName + ", Item not found for key", keyToCompare);
      } else {
        console.log('Item found', itemToSelect);
      }
      this.emotionForm.controls[controlName].setValue(itemToSelect);
    });
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

  // TODO: Remove this method, detection is on display emotion
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
    console.log('Destroying emotion-analyzer component');
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
    this.setSelectOptionsForEditAccordingToExistingValues(existingDetectionResult);
  }

  onCancel(): void {
    this.emotionForm.reset();
    this.dialogRef.close();
  }

  private setSelectOptionsForEditAccordingToExistingValues(emotionFromResult: EmotionFromNoteResult) {
    this.emotionTypeOptions?.changes?.subscribe((options: QueryList<MatOption>) => {
      const itemToSelect = this.emotionTypeOptions.find(option => {
          return option.value === emotionFromResult.emotionDetection?.emotionType;
        }
      );
      if (itemToSelect) {
        Promise.resolve().then(() => itemToSelect.select());
        console.log('Emotion type selected', itemToSelect);
      } else {
        console.warn(`Associated emotion ${emotionFromResult.emotionDetection?.emotionType} not found in the list of emotions.`);
      }
    });

    this.emotionOptions?.changes?.subscribe((options: QueryList<MatOption>) => {
        const itemToSelect = this.emotionOptions.find(option => {
            return option.value.emotion.id === emotionFromResult.emotionDetection?.mainEmotionId;
          }
        );
        if (itemToSelect) {
          Promise.resolve().then(() => itemToSelect.select());
          console.log('Emotion selected', itemToSelect);
        } else {
          console.warn(`Associated emotion ${emotionFromResult.emotionDetection?.mainEmotionId} not found in the list of emotions.`);
        }
      }
    );

    this.subEmotionOptions?.changes?.subscribe((options: QueryList<MatOption>) => {
        const itemToSelect = this.subEmotionOptions.find(option => {
            return option.value.subEmotionName === emotionFromResult.emotionDetection?.subEmotionId;
          }
        );
        if (itemToSelect) {
          Promise.resolve().then(() => itemToSelect.select());
          console.log('Sub emotion selected', itemToSelect);
        } else {
          console.warn(`Associated emotion ${emotionFromResult.emotionDetection?.subEmotionId} not found in the list of emotions.`);
        }
      }
    );

    this.triggerOptions?.changes?.subscribe((options: QueryList<MatOption>) => {
        const itemToSelect = this.triggerOptions.find(option => {
            return option.value.triggerId === emotionFromResult.emotionDetection?.triggers[0]?.triggerId;
          }
        );
        if (itemToSelect) {
          Promise.resolve().then(() => itemToSelect.select());
          console.log('Trigger selected', itemToSelect);
        } else {
          console.warn(`Associated trigger ${emotionFromResult.emotionDetection?.triggers[0]?.triggerId} not found in the list of triggers.`);
        }
      }
    );
  }

  getEmotionTypes(): Observable<string[]> {
    return this.emotionCacheService.emotionCache$.pipe(
      map((cachedEmotionData) => {
          if (cachedEmotionData) {
            return cachedEmotionData.emotionTypes.map(emotionTypeObject => emotionTypeObject.emotionType);
          } else {
            return [];
          }
        }
      ));
  }

  private getMainEmotions(): Observable<EmotionWithSubEmotions[]> | undefined {
    return this.emotionForm.get('emotionType')?.valueChanges.pipe(
      switchMap((selectedEmotionType) =>
        this.emotionCacheService.emotionCache$.pipe(
          map((cachedEmotionData) => {
            if (cachedEmotionData && selectedEmotionType) {
              const emotionTypeObject = cachedEmotionData.emotionTypes.find(
                emotionTypeObject => emotionTypeObject.emotionType === selectedEmotionType
              );
              return emotionTypeObject ? emotionTypeObject.emotions : [];
            } else {
              return [];
            }
          })
        )
      )
    );
  }

  getSubEmotions(): Observable<SubEmotionWrapper[]> | undefined {
    return this.emotionForm.get('emotion')?.valueChanges.pipe(
      switchMap((selectedEmotion) => {
          if (selectedEmotion) {
            return of(selectedEmotion.subEmotions);
          } else {
            return of([] as SubEmotionWrapper[]);
          }
        }
      )
    );
  }

  private getTriggers(): Observable<Trigger[]> | undefined {
    return this.emotionForm.get('emotion')?.valueChanges.pipe(
      switchMap((selectedEmotion) => {
          return this.emotionCacheService.emotionCache$.pipe(
            map((cachedEmotionData) => {
              if (cachedEmotionData && selectedEmotion) {
                return cachedEmotionData.triggers;
              } else {
                return [];
              }
            })
          );
        }
      )
    );
  }
}
