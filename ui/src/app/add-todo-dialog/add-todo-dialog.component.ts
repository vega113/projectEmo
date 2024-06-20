import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {UserTodo} from '../models/emotion.model';
import {FormControl, FormGroup, Validators} from "@angular/forms";

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

    todoForm: FormGroup;

    constructor(public dialogRef: MatDialogRef<AddTodoDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: any) {
        this.todoForm = new FormGroup({
            'title': new FormControl('', Validators.required),
            'description': new FormControl('')
        });
    }

    ngOnInit() {
        if (this.data?.todo) {
            this.todo = this.data.todo;
            this.isNewTodo = false;
            this.todoForm.setValue({title: this.todo.title, description: this.todo.description});
        } else {
            this.todo = {
                isAi: false,
                isDeleted: false,
                isRead: false,
                title: '', description: '', isDone: false, isArchived: false
            }
        }
    }

    onSave() {
        this.todo.title = this.todoForm.value.title;
        this.todo.description = this.todoForm.value.description;
        this.dialogRef.close(this.todo);
    }

    onCancel() {
        this.dialogRef.close(false);
    }

  onTranscriptionReady(transcription: string, field: string) {
    const current = this.todoForm.get(field)?.value || '';
    this.todoForm.get(field)?.setValue(`${current} ${transcription}`);
  }

}
