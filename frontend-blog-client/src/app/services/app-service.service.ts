import { Injectable } from '@angular/core';
import { RestClientService } from './rest-client.service';
import { JwtParserService } from './jwt-parser.service';
import { AuthenticationObjectService } from './authentication-object.service';
import { PostsCacheService } from './posts-cache.service';

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


  public registerUser() {

  }

  public authenticateUser() {

  }

  public getAuthentication() {

  }

  public getAuthenticationObservable() {

  }

  public getPosts() {

  }

  public newPost() {
    
  }

}
