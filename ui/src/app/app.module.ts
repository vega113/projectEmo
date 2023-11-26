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
import {MatTableModule} from "@angular/material/table";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatSortModule} from "@angular/material/sort";
import {ScreenSizeDirective} from "./directives/screenSize.directive";
import {LayoutModule} from "@angular/cdk/layout";
import { EmotionCalendarComponent } from './emotion-calendar/emotion-calendar.component';
import {MatDatepickerModule} from "@angular/material/datepicker";
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE} from "@angular/material/core";
import {DateFnsAdapter} from "./adapters/date-fns.adapter";
import { DayComponent } from './day/day.component';
import {MatGridListModule} from "@angular/material/grid-list";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatTabsModule} from "@angular/material/tabs";
import {NgxChartsModule} from "@swimlane/ngx-charts";
import { CustomDatePickerComponent } from './custom-date-picker/custom-date-picker.component';
import {MatSlideToggleModule} from "@angular/material/slide-toggle";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatExpansionModule} from "@angular/material/expansion";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import { CanvasJSAngularChartsModule } from '@canvasjs/angular-charts';
import {MatRadioModule} from "@angular/material/radio";
import { DayInfoDialogComponent } from './day-info-dialog/day-info-dialog.component';
import {MatDialogModule} from "@angular/material/dialog";
import { NoteTodoComponent } from './note-todo/note-todo.component';
import { UserTodosComponent } from './user-todos/user-todos.component';
import {MatCheckboxModule} from "@angular/material/checkbox";
import { AddTodoDialogComponent } from './add-todo-dialog/add-todo-dialog.component';
import {MatMenuModule} from "@angular/material/menu";

export const MAT_DATE_FNS_FORMATS = {
  parse: {
    dateInput: 'MM/dd/yyyy',
  },
  display: {
    dateInput: 'MM/dd/yyyy',
    monthYearLabel: 'MMM yyyy',
    dateA11yLabel: 'LL',
    monthYearA11yLabel: 'MMMM yyyy',
  },
};

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
    DashboardComponent,
    ScreenSizeDirective,
    EmotionCalendarComponent,
    DayComponent,
    CustomDatePickerComponent,
    DayInfoDialogComponent,
    NoteTodoComponent,
    UserTodosComponent,
    AddTodoDialogComponent,
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
        MatProgressSpinnerModule,
        MatTableModule,
        MatPaginatorModule,
        MatSortModule,
        LayoutModule,
        MatDatepickerModule,
        MatGridListModule,
        MatTooltipModule,
        MatTabsModule,
        NgxChartsModule,
        MatSlideToggleModule,
        MatAutocompleteModule,
        MatExpansionModule,
        MatProgressBarModule,
        CanvasJSAngularChartsModule,
        MatRadioModule,
        MatDialogModule,
        MatCheckboxModule,
        MatMenuModule

    ],
  providers: [{ provide: DateAdapter, useClass: DateFnsAdapter }, {
    provide: DateAdapter,
    useClass: DateFnsAdapter,
    deps: [MAT_DATE_LOCALE],
  },
    {
      provide: MAT_DATE_FORMATS,
      useValue: MAT_DATE_FNS_FORMATS
    }],
  bootstrap: [AppComponent]
})
export class AppModule { }
