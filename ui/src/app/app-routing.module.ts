import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MainLayoutComponent } from './main-layout/main-layout.component';
import { LandingPageComponent } from './landing-page/landing-page.component';
import { LoginComponent } from './login/login.component';
import { SignupComponent } from './signup/signup.component';
import {CreateEmotionComponent} from "./create-emotion/create-emotion.component";
import {EmotionsTimelineComponent} from "./emotions-timeline/emotions-timeline.component";
import {DashboardComponent} from "./dashboard/dashboard.component";
import { AuthGuard } from './services/auth.guard';
import {DisplayEmotionComponent} from "./display-emotion/display-emotion.component";
import { MatListModule } from '@angular/material/list';
import {EmotionCalendarComponent} from "./emotion-calendar/emotion-calendar.component";

export const routes: Routes = [
  { path: '', redirectTo: 'emotions-timeline', pathMatch: 'full' },
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      { path: 'landing', component: LandingPageComponent },
      { path: 'login', component: LoginComponent },
      { path: 'signup', component: SignupComponent },
      { path: 'create-emotion', component: CreateEmotionComponent , canActivate: [AuthGuard] },
      { path: 'emotions-timeline', component: EmotionsTimelineComponent , canActivate: [AuthGuard] },
      { path: 'charts', component: DashboardComponent, canActivate: [AuthGuard] },
      { path: 'display-emotion', component: DisplayEmotionComponent , canActivate: [AuthGuard]},
      { path: 'emotions-calendar', component: EmotionCalendarComponent, canActivate: [AuthGuard] },
    ],
  },
];
@NgModule({
  imports: [RouterModule.forRoot(routes), MatListModule],
  exports: [RouterModule],
})
export class AppRoutingModule {}
