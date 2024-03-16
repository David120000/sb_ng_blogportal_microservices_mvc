import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AuthRequest } from '../models/auth-request';
import { AuthToken } from '../models/auth-token';
import { Observable, catchError, throwError } from 'rxjs';
import { User } from '../models/user';
import { NewPostDTO } from '../models/new-post-dto';
import { Post } from '../models/post';
import { PostPages } from '../models/post-pages';
import { UserProfileDTO } from '../models/user-profile-dto';

@Injectable({
  providedIn: 'root'
})
export class RestClientService {

  private readonly BACKEND_URL: string;

  constructor(private http: HttpClient) { 
    this.BACKEND_URL = "http://localhost:8080";
  }


  public register(user: User): Observable<any> {

    return this.http.post(
        this.BACKEND_URL + "/user/new", 
        user
      )
      .pipe(catchError(this.handleGeneralError));
  }

  public authenticate(authRequest: AuthRequest): Observable<AuthToken> {

    return this.http.post<AuthToken>(
        this.BACKEND_URL + "/authenticate",
        authRequest
      )
      .pipe(catchError(this.handleAuthenticationError));
  }

  public newPost(post: NewPostDTO, securityToken: AuthToken): Observable<Post> {

    let headers = this.generateAuthorizationHeader(securityToken);

    let response = this.http.post<Post>(
        this.BACKEND_URL + "/post/new",
        post,
        {headers: headers}
      )
      .pipe(catchError(this.handleGeneralError));

    return response;
  }

  public getPosts(
        pageNumber: number, 
        pageSize: number, 
        securityToken: AuthToken, 
        authorEmail?: string, 
        includeNonPublished?: boolean
    ): Observable<PostPages> {

    let headers = this.generateAuthorizationHeader(securityToken);

    let params = new HttpParams()
      .set("pageNumber", pageNumber)
      .set("pageSize", pageSize);

    if(authorEmail && authorEmail.length > 0) {
      params = params.set("authorEmail", authorEmail);
    }

    if(includeNonPublished) {
      params = params.set("includeNonPublished", includeNonPublished);
    }

    let response = this.http.get<PostPages>(
        this.BACKEND_URL + "/post/get",
        {headers: headers, params: params}
      )
      .pipe(catchError(this.handleGeneralError));

    return response;
  }

  public deletePost(postId: string, securityToken: AuthToken): Observable<any> {

    let headers = this.generateAuthorizationHeader(securityToken);
    let response = this.http.delete(
      this.BACKEND_URL + '/post/delete/' + postId,
      {headers: headers}
    )
    .pipe(catchError(this.handleGeneralError));

    return response;
  }

  public updatePost(post: Post, securityToken: AuthToken): Observable<Post> {
    
    let headers = this.generateAuthorizationHeader(securityToken);
    let response = this.http.put<Post>(
      this.BACKEND_URL + '/post/update',
      post,
      {headers: headers}
    )
    .pipe(catchError(this.handleGeneralError));

    return response;
  }

  public getProfile(userEmail: string, securityToken: AuthToken): Observable<UserProfileDTO> {

    let headers = this.generateAuthorizationHeader(securityToken);

    return this.http.get<UserProfileDTO>(
        this.BACKEND_URL + "/user/profile/" + userEmail,
        {headers: headers}
      )
      .pipe(catchError(this.handleGeneralError));
  }


  private generateAuthorizationHeader(securityToken: AuthToken): HttpHeaders {

    let authorization = '';
    if(securityToken.jwt != undefined) {
      authorization = securityToken.jwt;
    }

    return new HttpHeaders()
      .set("Content-Type", "application/json; charset=utf-8")
      .set("Authorization", authorization);
  }


  private handleGeneralError(error: HttpErrorResponse) {

    let errorText = "An error occured: ";

    if(error.status === 0) {
      errorText += "client-side or network error.";
    }
    else {
      errorText += error.error;
    }

    return throwError(() => new Error(errorText));
  }

  private handleAuthenticationError(error: HttpErrorResponse) {

    let errorText = "An error occured: ";

    if(error.status === 0) {
      errorText += "client-side or network error.";
    }
    else if(error.status === 401) {
      errorText += "wrong password.";
    }
    else if(error.status === 400) {
      errorText += "user not found."
    }
    else {
      errorText += error.error;
    }

    return throwError(() => new Error(errorText));
  }

}
