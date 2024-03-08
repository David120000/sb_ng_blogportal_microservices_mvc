import { Injectable } from '@angular/core';
import { RestClientService } from './rest-client.service';
import { JwtParserService } from './jwt-parser.service';
import { AuthenticationObjectService } from './authentication-object.service';
import { PostsCacheService } from './posts-cache.service';
import { User } from '../models/user';
import { UserRegistrationDTO } from '../models/user-registration-dto';
import { Roles } from '../models/role-enum';
import { CompletionStatusInformation } from '../models/completion-status-information';
import { AuthenticatedUser } from '../models/authenticated-user';
import { Observable, Observer, firstValueFrom } from 'rxjs';
import { AuthRequest } from '../models/auth-request';
import { AuthToken } from '../models/auth-token';
import { NewPostDTO } from '../models/new-post-dto';
import { Post } from '../models/post';
import { PostPages } from '../models/post-pages';
import { UserProfileDTO } from '../models/user-profile-dto';

@Injectable({
  providedIn: 'root'
})
export class AppServiceService {

  private userProfileFetchSemafor: Array<string>

  constructor(
    private restClient: RestClientService, 
    private jwtParser: JwtParserService, 
    private authObjectService: AuthenticationObjectService, 
    private postsCache: PostsCacheService
    ) { 
      this.userProfileFetchSemafor = new Array<string>;
    }


  public async registerUser(userReg: UserRegistrationDTO): Promise<CompletionStatusInformation> {
    
    let user = new User(
      userReg.email,
      userReg.password,
      Roles.USER,
      true,
      userReg.firstName,
      userReg.lastName,
      userReg.about
    );

    return await firstValueFrom(
      new Observable((observer: Observer<CompletionStatusInformation>) =>
      
        this.restClient.register(user)
          .subscribe({
            next: (response) => {
              // the endpoint returns a 201 status with empty body hence the 'response' is null
              let result = new CompletionStatusInformation();
              result.executedSuccessfully = true;
              result.message = "Account registered successfully. Now you can log in.";
              observer.next(result);
            },
            error: (error) => {
              let result = new CompletionStatusInformation();
              result.executedSuccessfully = false;
              result.message = error.message;
              observer.next(result);
            },
            complete: () => console.log("User registration completed.")
          })
      )
    );
  }

  public async authenticateUser(authRequset: AuthRequest): Promise<CompletionStatusInformation> {

    return await firstValueFrom(
      new Observable((observer: Observer<CompletionStatusInformation>) =>

        this.restClient.authenticate(authRequset)
          .subscribe({
            next: (response) => {
              let token = Object.assign(new AuthToken(), response);
              let subjectId = this.jwtParser.getSubjectId(token);
              let authenticatedUser = new AuthenticatedUser(subjectId, token);

              this.authObjectService.setAuthentication(authenticatedUser);
  
              let completion = new CompletionStatusInformation()
              completion.executedSuccessfully = true;
              completion.message = "The credentials are correct. The authentication was successful.";
              observer.next(completion);
            },
            error: (error) => {
              let completion = new CompletionStatusInformation();
              completion.executedSuccessfully = false;
              completion.message = error.message;
              observer.next(completion);
            },
            complete: () => console.log("Authentication completed.")
          })

      )
    );
  }

  public clearAuthentication() {
    this.authObjectService.clearAuthentication();
  }

  public getAuthentication(): AuthenticatedUser {
    return this.authObjectService.getAuthentication();
  }

  public getAuthenticationObservable(): Observable<AuthenticatedUser> {
    return this.authObjectService.getAuthenticationObservable();
  }

  public async fetchPosts(pageNumber: number, pageSize: number, authorEmail?: string, includeNonPublished?: boolean): Promise<CompletionStatusInformation> {
    
    let securityToken = this.getSecurityToken();

    return await firstValueFrom(
      new Observable((observer: Observer<CompletionStatusInformation>) => {
        
        let response = 
        (authorEmail != undefined && includeNonPublished != undefined) ? this.restClient.getPosts(pageNumber, pageSize, securityToken, authorEmail, includeNonPublished) :
        (authorEmail != undefined && includeNonPublished == undefined) ? this.restClient.getPosts(pageNumber, pageSize, securityToken, authorEmail) :
        this.restClient.getPosts(pageNumber, pageSize, securityToken);

        response.subscribe({
          next: (response) => {
            let postPages = Object.assign(new PostPages(), response);
            let content = postPages.content;

            if(content != undefined) {
              for(let i = 0; i < content.length; i++) {
                this.postsCache.addToPostsCache(content.at(i)!);
              }
            }
            
            let result = new CompletionStatusInformation();

            if(postPages.empty === true && postPages.totalElements != undefined) {
              
              result.executedSuccessfully = false;
              if(postPages.totalElements === 0) {
                result.message = "There are no posts yet.";
              }
              else {
                result.message = "You have reached the end of the posts.";
              }
            }
            else if(postPages.numberOfElements != undefined && postPages.numberOfElements > 0) {

              result.executedSuccessfully = true;
              result.message = "Posts fetched.";
            }
            
            observer.next(result);
          },
          error: (error) => {

            let result = new CompletionStatusInformation();
            result.executedSuccessfully = false;

            if(error.status === 401 || error.status === 403) {
              this.authObjectService.clearAuthentication();
              result.message = "Your authentication has expired or the token was corrupted. Please log in again.";
            }
            else {
              result.message = error.message;
            }

            observer.next(result);
          },
          complete: () => console.log("Downloading posts completed.")
        });
      })
    );
  }

  public getPosts(): Array<Post> {
    return this.postsCache.getPosts();
  }

  public getProfileByEmail(email: string): UserProfileDTO {

    let authorProfile = this.postsCache.getAuthor(email);

    if(authorProfile === undefined) {
      authorProfile = new UserProfileDTO();
      this.fetchProfileByEmail(email);
    }

    return authorProfile;
  }

  public async newPost(newPost: NewPostDTO): Promise<CompletionStatusInformation> {

    let securityToken = this.getSecurityToken();

    return await firstValueFrom(
      new Observable((observer: Observer<CompletionStatusInformation>) =>
        
        this.restClient.newPost(newPost, securityToken)
          .subscribe({
            next: (response) => {
              let persistedPost = Object.assign(new Post(), response);
              this.postsCache.addFirstToPostsCache(persistedPost);
              
              let result = new CompletionStatusInformation();
              result.executedSuccessfully = true;
              result.message = "Post saved.";
              observer.next(result);
            },
            error: (error) => {
              let result = new CompletionStatusInformation();
              result.executedSuccessfully = false;
              result.message = error.message;
              observer.next(result);
            },
            complete: () => console.log("NewPost process completed.")
          })
      )
    );
  }

  private fetchProfileByEmail(email: string) {

    if(!this.userProfileFetchSemafor.includes(email)) {
      
      this.userProfileFetchSemafor.push(email);

      let securityToken = this.getSecurityToken();

      this.restClient.getProfile(email, securityToken)
        .subscribe(response => {
          let authorProfile = Object.assign(new UserProfileDTO, response);
          this.postsCache.addToAuthorsCache(authorProfile);
        });
    }
  }

  private getSecurityToken(): AuthToken {
    return (this.authObjectService.getAuthentication().securityToken) ? this.authObjectService.getAuthentication().securityToken! : new AuthToken();
  }

}
