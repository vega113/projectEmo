import {Component, Input} from '@angular/core';
import {NoteTodo} from '../models/emotion.model';
import {NoteTodoService} from "../services/note-todo.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {catchError} from "rxjs/operators";
import {of} from "rxjs";

@Component({
  selector: 'app-note-todo',
  templateUrl: './note-todo.component.html',
  styleUrls: ['./note-todo.component.css']
})
export class NoteTodoComponent {
  @Input() noteTodo: NoteTodo | null = null;
  isSavingStatus: boolean = false

  constructor(private noteTodoService: NoteTodoService, private snackBar: MatSnackBar) {

  }

    acceptTodo(): void {
        if (this.noteTodo != null) {
            this.isSavingStatus = true;
            console.log('Accepting todo', this.noteTodo);
            this.noteTodoService.updateNoteTodoToAccepted({
                id: this.noteTodo.id!,
                isAccepted: true
            }).pipe(
                catchError((error) => {
                    console.error('Error updating NoteTodo:', error);
                    this.isSavingStatus = false;
                    this.snackBar.open('An error occurred while updating the todo', 'Close', {
                        panelClass: 'snackbar-error',
                        duration: 2000,
                    });
                    return of(null);
                })
            ).subscribe((success) => {
                if (success) {
                    console.log('Todo accepted');
                    this.isSavingStatus = false;
                    this.noteTodo!.isAccepted = true;
                    this.snackBar.open('Todo accepted', 'Close', {
                        duration: 3000
                    });
                } else {
                    console.log('Todo not accepted');
                    this.isSavingStatus = false;
                    this.snackBar.open('Failed to save todo status', 'Close', {
                        duration: 3000
                    });
                }
            });
        }
    }
}
