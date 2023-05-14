import { DateAdapter } from '@angular/material/core';
import { format, getDay, getDate, getDaysInMonth, getMonth, getYear, isValid, isDate, addYears, addMonths, addDays, parseISO } from 'date-fns';
import { Inject, Injectable, Optional } from '@angular/core';
import { MAT_DATE_LOCALE } from '@angular/material/core';

@Injectable()
export class DateFnsAdapter extends DateAdapter<Date> {
  constructor(@Optional() @Inject(MAT_DATE_LOCALE) matDateLocale: string) {
    super();
    this.setLocale(matDateLocale || 'en-US');
  }

  getYear(date: Date): number {
    return getYear(date);
  }

  getMonth(date: Date): number {
    return getMonth(date);
  }

  getDate(date: Date): number {
    return getDate(date);
  }

  getDayOfWeek(date: Date): number {
    return getDay(date);
  }

  getMonthNames(style: 'long' | 'short' | 'narrow'): string[] {
    // This method would need a locale aware implementation
    // Not provided in date-fns
    return ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
  }

  getDateNames(): string[] {
    // This method would need a locale aware implementation
    // Not provided in date-fns
    return Array.from({length: 31}, (_, i) => String(i + 1));
  }

  getDayOfWeekNames(style: 'long' | 'short' | 'narrow'): string[] {
    // This method would need a locale aware implementation
    // Not provided in date-fns
    return ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
  }

  getYearName(date: Date): string {
    return this.getYear(date).toString();
  }

  getFirstDayOfWeek(): number {
    // This method would need a locale aware implementation
    // Not provided in date-fns
    return 0; // Sunday
  }

  getNumDaysInMonth(date: Date): number {
    return getDaysInMonth(date);
  }

  clone(date: Date): Date {
    return new Date(date.getTime());
  }

  createDate(year: number, month: number, date: number): Date {
    return new Date(year, month, date);
  }

  today(): Date {
    return new Date();
  }

  parse(value: any, parseFormat: any): Date | null {
    return isDate(value) ? value : parseISO(value);
  }

  format(date: Date, displayFormat: any): string {
    return format(date, displayFormat);
  }

  addCalendarYears(date: Date, years: number): Date {
    return addYears(date, years);
  }

  addCalendarMonths(date: Date, months: number): Date {
    return addMonths(date, months);
  }

  addCalendarDays(date: Date, days: number): Date {
    return addDays(date, days);
  }

  toIso8601(date: Date): string {
    return date.toISOString();
  }

  isDateInstance(obj: any): boolean {
    return isDate(obj);
  }

  isValid(date: Date): boolean {
    return isValid(date);
  }

  invalid(): Date {
    return new Date(NaN);
  }
}
