import {Component, Input, Output, EventEmitter} from '@angular/core';
import {UserTodo} from '../models/emotion.model';
import {MatDialog} from "@angular/material/dialog";
import {AddTodoDialogComponent} from "../add-todo-dialog/add-todo-dialog.component";
import { trigger, state, style, animate, transition } from '@angular/animations';

@Component({
    selector: 'app-todo-tab',
    templateUrl: './todo-tab.component.html',
    styleUrls: ['./todo-tab.component.css'],
  animations: [
    trigger('checkUncheck', [
      state('checked', style({
        transform: 'scale(1)',
        color: 'green'
      })),
      state('unchecked', style({
        transform: 'scale(1)',
        color: 'black'
      })),
      transition('checked <=> unchecked', [
        animate('0.5s')
      ])
    ])
  ]
})
export class TodoTabComponent {
    @Input() label!: string;
    @Input() todos!: UserTodo[];
    @Output() complete = new EventEmitter<UserTodo>();
    @Output() archive = new EventEmitter<UserTodo>();
    @Output() delete = new EventEmitter<UserTodo>();
    @Output() edit = new EventEmitter<UserTodo>();
    @Input() postponeActionTitle!: (isArchived: boolean) => string;
    @Input() completeActionTitle!: (isDone: boolean) => string;

    constructor(public dialog: MatDialog) {
    }

    editTodo(todo: UserTodo): void {
        this.edit.emit(todo);
    }

    openEditTodoDialog(todoToBeUpdated: UserTodo): void {
        console.log("openEditTodoDialog", todoToBeUpdated);
        const dialogRef = this.dialog.open(AddTodoDialogComponent, {
            width: '45%', height: '45%',
            data: {todo: todoToBeUpdated}
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                console.log("openEditTodoDialog result", result);
                this.editTodo(result);
            }
        });
    }

  computeIcon(isDone: boolean) {
    return isDone ? "cancel_presentation" : "check" ;
  }

  handleClick(event: Event, emitter: EventEmitter<any>, todo: UserTodo): void {
    event.stopPropagation();
    emitter.emit(todo);
  }
}
