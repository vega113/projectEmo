import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DayInfoDialogComponent } from './day-info-dialog.component';

describe('DayInfoDialogComponent', () => {
  let component: DayInfoDialogComponent;
  let fixture: ComponentFixture<DayInfoDialogComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DayInfoDialogComponent]
    });
    fixture = TestBed.createComponent(DayInfoDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
