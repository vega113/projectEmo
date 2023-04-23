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


@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    LandingPageComponent,
    SignupComponent
  ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        FormsModule,
      HttpClientModule,
      ReactiveFormsModule,
      BrowserAnimationsModule
    ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
