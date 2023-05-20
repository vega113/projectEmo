import {Component, forwardRef, Input} from '@angular/core';
import {ControlValueAccessor, FormControl, NG_VALUE_ACCESSOR} from '@angular/forms';
import {DateAdapter, NativeDateAdapter, MAT_DATE_FORMATS} from '@angular/material/core';

export class CustomDateAdapter extends NativeDateAdapter {
  override getDayOfWeekNames(style: 'long' | 'short' | 'narrow'): string[] {
    return ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  }
}

export const MY_DATE_FORMATS = {
  parse: {
    dateInput: 'LL',
  },
  display: {
    dateInput: 'LL',
    monthYearLabel: 'MMM YYYY',
    dateA11yLabel: 'LL',
    monthYearA11yLabel: 'MMMM YYYY',
    weekday: 'short'
  },
};

@Component({
  selector: 'app-custom-date-picker',
  templateUrl: './custom-date-picker.component.html',
  styleUrls: ['./custom-date-picker.component.css'],
  providers: [
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: MY_DATE_FORMATS },
    { provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => CustomDatePickerComponent), multi: true }
  ]
})
export class CustomDatePickerComponent implements ControlValueAccessor {
  @Input() placeholder: string = '';
  control: FormControl = new FormControl(null);

  private onChange: (value: Date) => void = () => {};
  private onTouched: () => void = () => {};

  constructor() {
    this.control.valueChanges.subscribe(value => {
      this.onChange(value);
    });
  }

  writeValue(obj: any): void {
    if (obj) {
      this.control.setValue(obj, {emitEvent: false});
    }
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState?(isDisabled: boolean): void {
    isDisabled ? this.control.disable() : this.control.enable();
  }
}

