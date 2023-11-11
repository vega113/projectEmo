import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import { Component, Inject } from "@angular/core";
import { MatTableDataSource } from "@angular/material/table";
import {ColorService} from "../services/color.service";

@Component({
  selector: 'app-day-info-dialog',
  templateUrl: './day-info-dialog.component.html',
  styleUrls: ['./day-info-dialog.component.css']
})
export class DayInfoDialogComponent {

  displayedColumns: string[] = ['date',  'intensity', 'note'];
  records: MatTableDataSource<any>;

  constructor(@Inject(MAT_DIALOG_DATA) public data: any,
              private dialogRef: MatDialogRef<DayInfoDialogComponent>,
              private colorService: ColorService
              ) {
    const dataArr: any[] = [];  // This will hold our record data

    if (this.data.day) {
      const day = this.data.day;

      for (let i = 0; i < day.records.length; i++) {
        let record = day.records[i];
        let recordInfo = {
          date: record.created,
          associatedEmotion: record.subEmotions?.length > 0 ? record.subEmotions[0].subEmotionName : null,
          emotion: record.emotion?.emotionName,
          intensity: record.intensity,
          trigger: record.triggers.length > 0 ? record.triggers[0].triggerName : null,
          note: record.notes.length > 0 ? record.notes[0]?.title : null,
          color: this.colorService.getColorForEmotionType(record.emotionType,
            true)
        };
        console.log("day recordInfo: ", recordInfo);
        dataArr.push(recordInfo);
      }
    }

    this.records = new MatTableDataSource(dataArr);  // Assign the data array to MatTableDataSource
  }

  get moreThanFiveRecords() {
    return this.data.day && this.data.day.records.length > 5;
  }

  closeDialog() {
    this.dialogRef.close();
  }
}
