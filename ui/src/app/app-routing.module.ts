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

const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      { path: '', component: LandingPageComponent },
      { path: 'login', component: LoginComponent },
      { path: 'signup', component: SignupComponent },
      { path: 'create-emotion', component: CreateEmotionComponent , canActivate: [AuthGuard] },
      { path: 'emotions-timeline', component: EmotionsTimelineComponent , canActivate: [AuthGuard] },
      { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
      { path: 'display-emotion', component: DisplayEmotionComponent },
    ],
  },
];
@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
