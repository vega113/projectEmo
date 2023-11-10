import {Injectable} from '@angular/core';
import {zonedTimeToUtc} from 'date-fns-tz';
import { utcToZonedTime, format } from 'date-fns-tz';
import {endOfMonth, startOfMonth} from "date-fns";

@Injectable({
  providedIn: 'root'
})
export class DateService {

  constructor() {
  }

  formatDateToIsoString(date: Date): string {
    // Get the user's time zone
    const timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;

    // Convert the local date to a UTC date
    const utcDate = zonedTimeToUtc(date, timeZone);

    // Format the UTC date as an ISO string including time
    return format(utcDate, "yyyy-MM-dd'T'HH:mm:ss'Z'");
  }

  formatDateFromDb(dateString: string): string {
    // Get the user's time zone
    const timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;

    // Convert the UTC date to the user's local date
    const localDate = utcToZonedTime(new Date(dateString), timeZone);

    // Format the local date including time
    return format(localDate, 'yyyy-MM-dd HH:mm:ss', {timeZone});
  }


  formatDateToIsoMonthStartEndString(date: Date, dateFn: (date: Date) => Date): string {
    // Get the user's time zone
    const timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;

    // Apply the passed function (like startOfMonth or endOfMonth) to the date
    const adjustedDate = dateFn(date);

    // Format the date as an ISO string including time and time zone
    return format(adjustedDate, "yyyy-MM-dd'T'HH:mm:ssXXX", {timeZone: timeZone});
  }

  createThreeMonthDateRangeFormattedStr(): { start: string, end: string } {
    const today = new Date();
    const threeMonthsAgo = new Date();
    threeMonthsAgo.setMonth(today.getMonth() - 3);

    return {
      start: this.formatDateToIsoMonthStartEndString(threeMonthsAgo, startOfMonth),
      end: this.formatDateToIsoMonthStartEndString(today, endOfMonth)
    };
  }

  createThreeMonthDateRange(): { start: Date, end: Date } {
    const today = new Date();
    const threeMonthsAgo = new Date();
    threeMonthsAgo.setMonth(today.getMonth() - 3);

    return {
      start: threeMonthsAgo,
      end: today
    };
  }


}
