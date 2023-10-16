import {Component, Input} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {EmotionService} from "../services/emotion.service";
import {EmotionStateService} from "../services/emotion-state.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {NoteService} from "../services/note.service";
import {DateService} from "../services/date.service";
import {EmotionRecord, Note} from "../models/emotion.model";

@Component({
  selector: 'app-emotion-note-form',
  templateUrl: './emotion-note-form.component.html',
  styleUrls: ['./emotion-note-form.component.css']
})
export class EmotionNoteFormComponent {

  @Input()   emotion: EmotionRecord | null = null;


  noteForm: FormGroup;

  isLoadingNotes: boolean = false;
  noteSaved: boolean = false;

  constructor(private fb: FormBuilder, private emotionService: EmotionService,
              private emotionStateService: EmotionStateService,
              private snackBar: MatSnackBar,
              private noteService: NoteService,
              public dateService: DateService) {
    this.noteForm = this.fb.group({
      note: ['', Validators.required]
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
      } else {
        // TODO: create emotion
      }
    }
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
