import { Component, EventEmitter, Output } from '@angular/core';
import { MediaRecorderService } from '../services/media-recorder.service';

@Component({
  selector: 'app-voice-recorder',
  templateUrl: './voice-recorder.component.html',
  styleUrls: ['./voice-recorder.component.css']
})
export class VoiceRecorderComponent {
  @Output() transcriptionReady = new EventEmitter<string>();
  isRecording = false;
  isTranscribing = false;

  constructor(private mediaRecorderService: MediaRecorderService) { }

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
    this.mediaRecorderService.transcribeAudio(audioData).subscribe(transcription => {
      this.transcriptionReady.emit(transcription.text);
      this.isTranscribing = false;
    });
  }
}
