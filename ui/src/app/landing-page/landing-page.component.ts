import {Component} from '@angular/core';

@Component({
  selector: 'app-landing-page',
  templateUrl: './landing-page.component.html',
  styleUrls: ['./landing-page.component.css']
})
export class LandingPageComponent {
  title = 'Welcome to the Landing Page!';

  changeTitle() {
    this.title = 'Welcome to the Updated Landing Page!';
  }
}
