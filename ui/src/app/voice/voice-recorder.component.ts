import { Component, EventEmitter, Output } from '@angular/core';
import { MediaRecorderService } from '../services/media-recorder.service';
import {MatSnackBar} from "@angular/material/snack-bar";

@Component({
  selector: 'app-voice-recorder',
  templateUrl: './voice-recorder.component.html',
  styleUrls: ['./voice-recorder.component.css']
})
export class VoiceRecorderComponent {
  @Output() transcriptionReady = new EventEmitter<string>();
  isRecording = false;
  isTranscribing = false;

  constructor(private mediaRecorderService: MediaRecorderService, private snackBar: MatSnackBar) { }

  startRecording() {
    this.isRecording = true;
    this.mediaRecorderService.startRecording();
  }

  stopRecording() {
    this.isRecording = false;
    this.isTranscribing = true;
    this.mediaRecorderService.stopRecording().then(audioData => {
      this.textToSpeech(audioData);
    });
  }

  textToSpeech(audioData: Blob) {
    this.mediaRecorderService.transcribeAudio(audioData).subscribe(
      transcription => {
        this.transcriptionReady.emit(transcription.text);
        this.isTranscribing = false;
      },
      error => {
        console.error('An error occurred:', error);
        this.isTranscribing = false;
        this.snackBar.open('An error occurred while transcribing the audio', 'Close', {
          panelClass: 'snackbar-error',
          duration: 5000,
        });
      }
    );
  }
}
