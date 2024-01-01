import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmotionAnalyzerComponent } from './emotion-analyzer.component';

describe('EmotionAnalyzerComponent', () => {
  let component: EmotionAnalyzerComponent;
  let fixture: ComponentFixture<EmotionAnalyzerComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [EmotionAnalyzerComponent]
    });
    fixture = TestBed.createComponent(EmotionAnalyzerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
