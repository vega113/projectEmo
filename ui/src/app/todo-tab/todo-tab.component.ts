import { Component, Input, Output, EventEmitter } from '@angular/core';
import { UserTodo } from '../models/emotion.model';

@Component({
  selector: 'app-todo-tab',
  templateUrl: './todo-tab.component.html',
  styleUrls: ['./todo-tab.component.css']
})
export class TodoTabComponent {
  @Input() label!: string;
  @Input() todos!: UserTodo[];
  @Output() complete = new EventEmitter<UserTodo>();
  @Output() archive = new EventEmitter<UserTodo>();
  @Input() postponeActionTitle!: (isArchived: boolean) => string;
}
