import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';


import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './login/login.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";


import { HttpClientModule } from '@angular/common/http';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import { LandingPageComponent } from './landing-page/landing-page.component';
import { SignupComponent } from './signup/signup.component';
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatCardModule} from "@angular/material/card";
import {MatButtonModule} from "@angular/material/button";
import {MatInputModule} from "@angular/material/input";
import {MatIconModule} from "@angular/material/icon";
import { MainLayoutComponent } from './main-layout/main-layout.component';
import { NavigationComponent } from './navigation/navigation.component';
import { CreateEmotionComponent } from './create-emotion/create-emotion.component';
import { DisplayEmotionComponent } from './display-emotion/display-emotion.component';
import { EmotionsTimelineComponent } from './emotions-timeline/emotions-timeline.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import {MatListModule} from "@angular/material/list";
import { MatSidenavModule } from '@angular/material/sidenav';
import {MatSelectModule} from "@angular/material/select";
import {MatSliderModule} from "@angular/material/slider";

import { MatSnackBarModule } from '@angular/material/snack-bar';
import {MatChipsModule} from "@angular/material/chips";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    LandingPageComponent,
    SignupComponent,
    MainLayoutComponent,
    NavigationComponent,
    CreateEmotionComponent,
    DisplayEmotionComponent,
    EmotionsTimelineComponent,
    DashboardComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    ReactiveFormsModule,
    BrowserAnimationsModule,
    MatToolbarModule,
    MatCardModule,
    MatButtonModule,
    MatInputModule,
    MatIconModule,
    MatListModule,
    MatSidenavModule,
    MatSelectModule,
    MatSliderModule,
    MatSnackBarModule,
    MatChipsModule,
    MatProgressSpinnerModule

  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
