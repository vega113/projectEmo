import { Component } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import {UserTodo} from "../models/emotion.model";

@Component({
  selector: 'app-add-todo-dialog',
  templateUrl: './add-todo-dialog.component.html',
  styleUrls: ['./add-todo-dialog.component.css']
})

export class AddTodoDialogComponent {
  newTodo: UserTodo = {
    isAi: false,
    isDeleted: false,
    isRead: false,
    title: '', description: '', isDone: false, isArchived: false };

  constructor(public dialogRef: MatDialogRef<AddTodoDialogComponent>) {}

  onAdd() {
    this.dialogRef.close(this.newTodo);
  }

  onCancel() {
    this.dialogRef.close();
  }
}
