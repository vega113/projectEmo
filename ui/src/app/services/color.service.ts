import {Injectable} from "@angular/core";
import {ThemePalette} from "@angular/material/core";

@Injectable({
  providedIn: 'root'
})
export class ColorService {
  getColorForEmotionType(emotionType: string, hasRecords: boolean) {
    // find the emotion with the highest intensity

    let color = 'grey';

    // Define the colors corresponding to the Angular Material theme colors
    const primaryColor = '#3f51b5'; // blue
    const accentColor = '#ffb74d'; // pink
    const warnColor = '#e57373'; // red
    const greyColor = '#D3D3D3'; // grey

    if(emotionType === 'Negative') {
      color = warnColor;
    } else if (emotionType === 'Positive') {
      color = primaryColor;
    } else if (emotionType === 'Neutral') {
      color = accentColor;
    }
    else if (!hasRecords) {
      color = greyColor;
    }
    return color;
  }

  getSliderColorThemePalette(emotionType: string): ThemePalette {
    switch (emotionType) {
      case 'Positive':
        return 'primary';
      case 'Neutral':
        return 'accent';
      case 'Negative':
        return 'warn';
      default:
        return undefined;
    }
  }
}
