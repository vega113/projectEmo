import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {EmotionService} from "../services/emotion.service";
import {EmotionStateService} from "../services/emotion-state.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {NoteService} from "../services/note.service";
import {DateService} from "../services/date.service";
import {Emotion, EmotionFromNoteResult, EmotionRecord, Note, Tag} from "../models/emotion.model";

@Component({
  selector: 'app-emotion-note-form',
  templateUrl: './emotion-note-form.component.html',
  styleUrls: ['./emotion-note-form.component.css']
})
export class EmotionNoteFormComponent implements OnInit {

  @Input() emotion: EmotionRecord | null = null;
  @Input() noteText: string | null = null;
  @Output() noteSubmitted = new EventEmitter<EmotionFromNoteResult>();


  noteForm: FormGroup;

  isLoadingNotes: boolean = false;
  noteSaved: boolean = false;

  placeHolderText: string = "Try to describe how this emotion is affecting your daily activities or your interactions with others. Are there any noticeable patterns or recurring events? How do you wish to feel instead? What steps do you think you could take to influence your emotional state? Remember, you can also use #hashtags to categorize or highlight key points in your note.";
  submitBtnTxt: string | null = null;


  constructor(private fb: FormBuilder, private emotionService: EmotionService,
              private emotionStateService: EmotionStateService,
              private snackBar: MatSnackBar,
              private noteService: NoteService,
              public dateService: DateService) {
    this.noteForm = this.fb.group({
      note: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    if (this.noteText) {
      this.noteForm.get('note')?.setValue(this.noteText);
    }
    console.log('Emotion in note form', this.emotion);
    this.submitBtnTxt = this.emotion ? "Save Note" : "Detect Emotion with AI";
  }

  async onSubmitNote(): Promise<void> {
    this.isLoadingNotes = true;

    if (this.noteForm.valid) {
      const note: Note = {
        text: this.noteForm.value.note,
      } as Note;
      if (this.emotion?.id) {
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
        this.noteText = note.text;
        this.noteService.detectEmotion(note.text).subscribe({
          next: (response) => {
            console.log('Emotion detected successfully', response);
            this.noteSubmitted.emit(response);
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
    }
  }

  deleteNote(note: Note): void {
    this.noteService.deleteNote(note.id!)
      .subscribe(isDeleted => {
        if (isDeleted) {
          if (this.emotion?.notes) {
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
