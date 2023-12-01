import { Component, OnInit } from '@angular/core';
import {UserTodo} from '../models/emotion.model';
import {UserTodoService} from "../services/user-todo.service";
import {MatDialog} from "@angular/material/dialog";
import {AddTodoDialogComponent} from "../add-todo-dialog/add-todo-dialog.component";
import {MatSnackBar, MatSnackBarRef, SimpleSnackBar} from "@angular/material/snack-bar";

@Component({
  selector: 'app-user-todos',
  templateUrl: './user-todos.component.html',
  styleUrls: ['./user-todos.component.css']
})
export class UserTodosComponent implements OnInit {
  todos: UserTodo[] = [];
  page = 0;
  size = 25;
  editingTodoId: number | null = null;
  activeTodos: UserTodo[] = [];
  completedTodos: UserTodo[] = [];
  allTodos: UserTodo[] = [];
  archivedTodos: UserTodo[] = [];
  isLoading = true;
  snackBarDuration = 10000;

  snackBarRef: MatSnackBarRef<SimpleSnackBar> | null = null;


  constructor(private userTodoService: UserTodoService, public dialog: MatDialog,
              private snackBar: MatSnackBar) { }

  ngOnInit(): void {
    this.fetchTodos();
  }

  fetchTodos(): void {
    this.userTodoService.fetchUserTodos(this.page, this.size).subscribe(todos => {
      this.todos = todos;
      this.refresh();
      this.isLoading = false;
    });
  }

  private refresh() {
    this.allTodos = this.todos.filter(todo => !todo.isArchived);
    this.activeTodos = this.allTodos.filter(todo => !todo.isDone);
    this.archivedTodos = this.todos.filter(todo => todo.isArchived && !todo.isDone);
    this.completedTodos = this.todos.filter(todo => todo.isDone);
  }

  complete(todo: UserTodo): void {
    this.editingTodoId = todo.id!;
    todo.isDone = !todo.isDone;
    setTimeout(
      () => {
        this.userTodoService.complete(todo).subscribe({
          next: todos => {
            this.todos = todos;
            this.editingTodoId = null;
            this.refresh();
            const action = todo.isDone ? 'completed' : 'un-completed';
            this.snackBar.open(`Todo ${action}: ${todo.title}`, 'Close', {
              duration: this.snackBarDuration,
            });
          },
          error: err => {
            const action = todo.isDone ? 'complete' : 'un-complete';
            this.handleError(err, `Failed to ${action} todo: ${todo.title}`)
          }
        });
      }, 500
    )
  }

  archive(todo: UserTodo): void {
    todo.isArchived = !todo.isArchived;
    this.userTodoService.archive(todo).subscribe({
      next: todos => {
        this.todos = todos;
        this.editingTodoId = null;
        this.refresh();
        const action = todo.isArchived ? 'postponed' : 'activated';
        this.snackBar.open(`Todo ${action}: ${todo.title}`, 'Close', {
          duration: this.snackBarDuration,
        });
      },
      error: err => {
        const action = todo.isDone ? 'archive' : 'activate';
        this.handleError(err, `Failed to ${action} todo ${todo.title}`)
      }
    });
  }

  delete(todo: UserTodo): void {
    this.snackBar.open(`Deleting todo: ${todo.title}`, 'Close', {
      duration: this.snackBarDuration,
    });
    todo.isDeleted = true;
    this.userTodoService.update(todo).subscribe({
      next: todos => {
        this.todos = todos;
        this.refresh();
        this.snackBarRef = this.snackBar.open(`Todo deleted: ${todo.title}`, 'Undo', {
          duration: this.snackBarDuration,
        });
        this.snackBarRef.onAction().subscribe(() => {
          todo.isDeleted = false;
          this.userTodoService.update(todo).subscribe({
            next: todos => {
              this.todos = todos;
              this.refresh();
            },
            error: err => this.handleError(err, `Failed to undo delete for todo: ${todo.title}`)
          });
        });
        this.snackBarRef.afterDismissed().subscribe(info => {
          if (!info.dismissedByAction) {
            this.userTodoService.delete(todo).subscribe({
              next: todos => {
                this.todos = todos;
                this.refresh();
              },
              error: err => this.handleError(err, `Failed to delete todo: ${todo.title}`)
            });
          }
        });
      },
      error: err => this.handleError(err, `Failed to delete todo: ${todo.title}`)
    });
  }

  update(todo: UserTodo): void {
    this.userTodoService.update(todo).subscribe({
      next: todos => {
        this.todos = todos;
        this.editingTodoId = null;
        this.refresh();
        this.snackBar.open(`Todo updated: ${todo.title}`, 'Close', {
          duration: this.snackBarDuration,
        });
      },
      error: err => this.handleError(err, `Failed to update todo: ${todo.title}`)
    });
  }

  postponeActionTitle(isArchived: boolean): string {
    return isArchived ? 'Activate' : 'Postpone';
  }

  completeActionTitle(isDone: boolean): string {
    return isDone ?  'Un-complete' : 'Mark completed';
  }

  openAddTodoDialog(): void {
    const dialogRef = this.dialog.open(AddTodoDialogComponent, {
      width: '45%', height: '45%'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.createTodo(result);
      }
    });
  }

  createTodo(newTodo: UserTodo) {
    console.log("adding new todo");
    this.userTodoService.add(newTodo).subscribe({
      next: todos => {
        this.todos = todos;
        this.refresh();
        this.snackBar.open(`Todo Added: ${newTodo.title}`, 'Close', {
          duration: this.snackBarDuration,
        });
      },
      error: err => this.handleError(err, `Failed to add todo: ${newTodo.title}`)
    })
  }

  private handleError(err: any, errorMsg = "Failed to complete action") {
    console.error(err);
    this.snackBar.open(errorMsg, 'Close', {
      duration: this.snackBarDuration,
    });
  }

  moveToTop(todo: UserTodo) {
    this.userTodoService.update(todo).subscribe({
      next: todos => {
        this.todos = todos;
        this.refresh();
        this.snackBar.open(`Todo moved to top: ${todo.title}`, 'Close', {
          duration: this.snackBarDuration,
        });
      },
      error: err => this.handleError(err, `Failed to move todo to top: ${todo.title}`)
    });
  }
}



