import {Component, Input} from '@angular/core';
import {DayOfWeek} from "../models/emotion.model";
import { DayInfoDialogComponent } from '../day-info-dialog/day-info-dialog.component';
import {MatDialog} from "@angular/material/dialog";

@Component({
  selector: 'app-day',
  templateUrl: './day.component.html',
  styleUrls: ['./day.component.css']
})
export class DayComponent {
  @Input() day: DayOfWeek | null = null;

  private hoverTimeout: any;


  constructor(public dialog: MatDialog) {}

  showDayInfo() {
    this.dialog.open(DayInfoDialogComponent, {
      data: {
        day: this.day,
        getAverageIntensity: () => this.getAverageIntensity()
      },
      hasBackdrop: true,       // this will show a backdrop behind the dialog (default is true)
      disableClose: false
    });
  }


  getColor() {
    return this.day?.dayColor
  }

  getAverageIntensity() {
    return this.day?.averageIntensity.toFixed(2)
  }


  onMouseLeave() {
    clearTimeout(this.hoverTimeout);
  }
}
