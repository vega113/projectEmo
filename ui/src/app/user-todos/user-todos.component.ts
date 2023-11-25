import { Component, OnInit } from '@angular/core';
import {UserTodo} from '../models/emotion.model';
import {UserTodoService} from "../services/user-todo.service";
import {MatDialog} from "@angular/material/dialog";
import {AddTodoDialogComponent} from "../add-todo-dialog/add-todo-dialog.component";
import {MatSnackBar} from "@angular/material/snack-bar";

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


  constructor(private userTodoService: UserTodoService, public dialog: MatDialog,
              private snackBar: MatSnackBar) { }

  ngOnInit(): void {
    this.fetchTodos();
  }

  fetchTodos(): void {
    this.userTodoService.fetchUserTodos(this.page, this.size).subscribe(todos => {
      this.todos = todos;
      this.refresh();
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
    this.userTodoService.complete(todo).subscribe({
      next: todos => {
        this.todos = todos;
        this.editingTodoId = null;
        this.refresh();
        this.snackBar.open('Todo Completed', 'Close', {
          duration: 5000,
        });
      },
      error: err => this.handleError(err, "Failed to complete todo")
    });
  }

  archive(todo: UserTodo): void {
    this.editingTodoId = todo.id!;
    todo.isArchived = !todo.isArchived;
    this.userTodoService.archive(todo).subscribe({
      next: todos => {
        this.todos = todos;
        this.editingTodoId = null;
        this.refresh();
        this.snackBar.open('Todo Archived', 'Close', {
          duration: 5000,
        });
      },
      error: err => this.handleError(err, "Failed to archive todo")
    });
  }

  onPageChange(event: any): void {
    this.page = event.pageIndex;
    this.size = event.pageSize;
    this.fetchTodos();
  }

  isArchivedToTitle(isArchived: boolean): string {
    return isArchived ? 'Unarchive' : 'Archive';
  }

  openAddTodoDialog(): void {
    const dialogRef = this.dialog.open(AddTodoDialogComponent, {
      width: '40%', height: '30%'
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
        this.snackBar.open('Todo Added', 'Close', {
          duration: 5000,
        });
      },
      error: err => this.handleError(err, "Failed to add todo")
    })
  }

  private handleError(err: any, errorMsg = "Failed to complete action") {
    console.error(err);
    this.snackBar.open(errorMsg, 'Close', {
      duration: 5000,
    });
  }
}



