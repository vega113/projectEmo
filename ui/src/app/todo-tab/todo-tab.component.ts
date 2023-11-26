// todo-tab.component.ts
import {Component, Input, Output, EventEmitter} from '@angular/core';
import {UserTodo} from '../models/emotion.model';
import {MatDialog} from "@angular/material/dialog";
import {AddTodoDialogComponent} from "../add-todo-dialog/add-todo-dialog.component";

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
    @Output() delete = new EventEmitter<UserTodo>();
    @Output() edit = new EventEmitter<UserTodo>();
    @Input() postponeActionTitle!: (isArchived: boolean) => string;

    constructor(public dialog: MatDialog) {
    }

    editTodo(todo: UserTodo): void {
        this.edit.emit(todo);
    }

    openEditTodoDialog(todoToBeUpdated: UserTodo): void {
        console.log("openEditTodoDialog", todoToBeUpdated);
        const dialogRef = this.dialog.open(AddTodoDialogComponent, {
            width: '35%', height: '40%',
            data: {todo: todoToBeUpdated}
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.editTodo(result);
            }
        });
    }
}
