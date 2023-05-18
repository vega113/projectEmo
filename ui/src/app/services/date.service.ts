import {Injectable} from '@angular/core';
import {zonedTimeToUtc} from 'date-fns-tz';
import { utcToZonedTime, format } from 'date-fns-tz';

@Injectable({
  providedIn: 'root'
})
export class DateService {

  constructor() {
  }

  // formatDateToIsoString(date: Date): string {
  //   return this.formatDateTimeToIsoString(date)
  // }

  // formatDateToIsoString(date: Date): string {
  //   const year = date.getFullYear();
  //   const month = date.getMonth() + 1;
  //   const day = date.getDate();
  //   return `${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}`;
  // }

  // formatDateTimeToIsoString(date: Date): string {
  //   const year = date.getFullYear();
  //   const month = date.getMonth() + 1;
  //   const day = date.getDate();
  //   const hours = date.getHours();
  //   const minutes = date.getMinutes();
  //   const seconds = date.getSeconds();
  //   const timezoneOffset = -date.getTimezoneOffset(); // Note: The getTimezoneOffset method returns the difference, in minutes, from local time to UTC. The sign is reversed to align with the ISO format.
  //   const timezoneOffsetHours = Math.floor(Math.abs(timezoneOffset) / 60);
  //   const timezoneOffsetMinutes = Math.abs(timezoneOffset) % 60;
  //   const timezoneOffsetSign = timezoneOffset >= 0 ? '+' : '-';
  //
  //   return `${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}T${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}${timezoneOffsetSign}${timezoneOffsetHours.toString().padStart(2, '0')}:${timezoneOffsetMinutes.toString().padStart(2, '0')}`;
  // }

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
    return format(localDate, 'yyyy-MM-dd HH:mm:ss', { timeZone });
  }




formatDateToIsoMonthStartEndString(date: Date, dateFn: (date: Date) => Date): string {
  // Get the user's time zone
  const timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;

  // Apply the passed function (like startOfMonth or endOfMonth) to the date
  const adjustedDate = dateFn(date);

  // Format the date as an ISO string including time and time zone
  return format(adjustedDate, "yyyy-MM-dd'T'HH:mm:ssXXX", { timeZone: timeZone });
}

}
