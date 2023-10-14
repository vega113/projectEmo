import {Component} from '@angular/core';
import {EmotionService} from "../services/emotion.service";
import {EmotionStateService} from "../services/emotion-state.service";
import {EmotionRecord, Note, NoteTemplate, SuggestedAction} from '../models/emotion.model';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MatSnackBar} from "@angular/material/snack-bar";
import {MatSelectChange} from "@angular/material/select";
import { NoteService } from '../services/note.service';
import {DateService} from "../services/date.service";

@Component({
  selector: 'app-display-emotion',
  templateUrl: './display-emotion.component.html',
  styleUrls: ['./display-emotion.component.css']
})
export class DisplayEmotionComponent {


  constructor(private fb: FormBuilder, private emotionService: EmotionService,
              private emotionStateService: EmotionStateService,
              private snackBar: MatSnackBar,
              private noteService: NoteService,
              public dateService: DateService) {
    this.noteForm = this.fb.group({
      note: ['', Validators.required]
    });
  }

  noteTemplates: NoteTemplate[] | null = null;


  isLoadingNotes: boolean = false;
  isLoadingActions: boolean = false;

  noteSaved: boolean = false;


  isLoading: boolean = true;


  noteForm: FormGroup;

  emotion: EmotionRecord | null = null;

  note: string = '';

  suggestedActions: SuggestedAction[] | null = null;

// Add this method to get suggested actions
  async getSuggestedActions(): Promise<void> {
    this.isLoadingActions = true;
    if (this.emotion != null && this.emotion.id != null) {
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
  }

  ngOnInit(): void {
    this.emotionStateService.newEmotionRecord$.subscribe((newEmotion) => {
      if (newEmotion) {
        this.emotion = newEmotion;
        this.isLoading = false;
        console.log('New emotion received:', newEmotion);
      }
    });

    this.noteService.getNoteTemplates().subscribe((noteTemplates) => {
      this.noteTemplates = noteTemplates;
      console.log('note templates received');
    });
  }

  async onSubmitNote(): Promise<void> {
    this.isLoadingNotes = true;

    if (this.noteForm.valid) {
      const note = {
        text: this.noteForm.value.note,
      } as Note;
      if(this.emotion?.id)
      {
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

  onTemplateSelected(event: MatSelectChange) {
    this.noteForm.get('note')?.setValue(event.value);
  }

  deleteNote(note: Note): void {
    this.noteService.deleteNote(note.id!)
      .subscribe(isDeleted => {
        if (isDeleted) {
          if(this.emotion?.notes) {
            this.emotion.notes = this.emotion.notes?.filter((n: Note) => {
              return n.id !== note.id;
            });
            this.emotionStateService.updateNewEmotion(this.emotion);
            this.noteForm.reset();
          }
        }
        console.log('Note deleted successfully');

      });
  }
}
