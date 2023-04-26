import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DisplayEmotionComponent } from './display-emotion.component';

describe('DisplayEmotionComponent', () => {
  let component: DisplayEmotionComponent;
  let fixture: ComponentFixture<DisplayEmotionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DisplayEmotionComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DisplayEmotionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
