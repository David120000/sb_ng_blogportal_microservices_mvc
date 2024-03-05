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
import { Observable } from 'rxjs';
import { AuthRequest } from '../models/auth-request';
import { AuthToken } from '../models/auth-token';

@Injectable({
  providedIn: 'root'
})
export class AppServiceService {

  constructor(
    private restClient: RestClientService, 
    private jwtParser: JwtParserService, 
    private authObjectService: AuthenticationObjectService, 
    private postsCache: PostsCacheService
    ) { }


  public registerUser(userReg: UserRegistrationDTO): CompletionStatusInformation {
    
    let user = new User(
      userReg.email,
      userReg.password,
      Roles.USER,
      true,
      userReg.firstName,
      userReg.lastName,
      userReg.about
    );

    let result = new CompletionStatusInformation();

    this.restClient.register(user)
      .subscribe({
        next: (response) => {
          if(response.status === 201) {
            result.executedSuccessfully = true;
            result.message = "Account registered successfully. Now you can log in.";
          }
        },
        error: (error) => {
          result.executedSuccessfully = false;
          result.message = error.message;
        },
        complete: () => console.log("User registration completed.")
      });

    return result;
  }

  public authenticateUser(authRequset: AuthRequest): CompletionStatusInformation {

    let result = new CompletionStatusInformation();

    this.restClient.authenticate(authRequset)
      .subscribe({
        next: (response) => {
          let token = Object.assign(new AuthToken(), response);
          let subjectId = this.jwtParser.getSubjectId(token);
          let authenticatedUser = new AuthenticatedUser(subjectId, token);

          this.authObjectService.setAuthentication(authenticatedUser);

          result.executedSuccessfully = true;
          result.message = "The credentials are correct. The authentication was successful.";
        },
        error: (error) => {
          result.executedSuccessfully = false;
          result.message = error.message;
        },
        complete: () => console.log("Authentication completed.")
      });

    return result;
  }

  public getAuthentication(): AuthenticatedUser {
    return this.authObjectService.getAuthentication();
  }

  public getAuthenticationObservable(): Observable<AuthenticatedUser> {
    return this.authObjectService.getAuthenticationObservable();
  }

  public getPosts() {

  }

  public newPost() {
    
  }

}
