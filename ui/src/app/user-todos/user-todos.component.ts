import { Component, OnInit } from '@angular/core';
import {UserTodo} from '../models/emotion.model';
import {UserTodoService} from "../services/user-todo.service";

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


  constructor(private userTodoService: UserTodoService) { }

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
    this.archivedTodos = this.todos.filter(todo => todo.isArchived);
    this.activeTodos = this.allTodos.filter(todo => !todo.isDone);
    this.completedTodos = this.allTodos.filter(todo => todo.isDone);
  }

  complete(todo: UserTodo): void {
    this.editingTodoId = todo.id!;
    todo.isDone = !todo.isDone;
    this.userTodoService.complete(todo).subscribe(todos => {
      this.todos = todos;
      this.editingTodoId = null;
      this.refresh();
    });
  }

  archive(todo: UserTodo): void {
    this.editingTodoId = todo.id!;
    todo.isArchived = !todo.isArchived;
    this.userTodoService.archive(todo).subscribe(todos => {
      this.todos = todos;
      this.editingTodoId = null;
      this.refresh();
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
}



