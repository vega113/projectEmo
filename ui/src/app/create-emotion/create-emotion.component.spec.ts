import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateEmotionComponent } from './create-emotion.component';

describe('CreateEmotionComponent', () => {
  let component: CreateEmotionComponent;
  let fixture: ComponentFixture<CreateEmotionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CreateEmotionComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreateEmotionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
