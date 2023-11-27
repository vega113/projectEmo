import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import { UserTodo } from '../models/emotion.model';

@Component({
  selector: 'app-add-todo-dialog',
  templateUrl: './add-todo-dialog.component.html',
  styleUrls: ['./add-todo-dialog.component.css']
})
export class AddTodoDialogComponent {
    isNewTodo = true;
    todo: UserTodo = {
    isAi: false,
    isDeleted: false,
    isRead: false,
    title: '', description: '', isDone: false, isArchived: false
  };

  constructor(public dialogRef: MatDialogRef<AddTodoDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: any) {}

  ngOnInit() {
    if (this.data?.todo) {
      this.todo = this.data.todo;
      this.isNewTodo = false;
    }
  }

  onSave() {
    this.dialogRef.close(this.todo);
  }

  onCancel() {
    this.dialogRef.close();
  }
}
