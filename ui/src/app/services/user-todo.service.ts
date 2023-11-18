import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {UserTodo} from '../models/emotion.model';
import {AuthService} from "./auth.service";
import {environment} from "../../environments/environment";

@Injectable({
    providedIn: 'root'
})
export class UserTodoService {

    constructor(private http: HttpClient, private authService: AuthService) {
    }

    fetchUserTodos(page: number, size: number): Observable<UserTodo[]> {
        const headers = this.authService.getAuthorizationHeader();
        return this.http.get<UserTodo[]>(`${environment.baseUrl}/user/todo/${page}/${size}`, {headers});
    }

    complete(todo: UserTodo) {
        const headers = this.authService.getAuthorizationHeader();
        return this.http.put<UserTodo[]>(`${environment.baseUrl}/user/todo/complete/${todo.id}/${todo.isDone}`, {},
            {headers});
    }

    archive(todo: UserTodo) {
        const headers = this.authService.getAuthorizationHeader();
        return this.http.put<UserTodo[]>(`${environment.baseUrl}/user/todo/archive/${todo.id}/${todo.isArchived}`, {},
            {headers});
    }
}
