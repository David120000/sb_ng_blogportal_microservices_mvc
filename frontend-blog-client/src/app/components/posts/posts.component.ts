import { Component, DoCheck, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { AuthenticatedUser } from 'src/app/models/authenticated-user';
import { Post } from 'src/app/models/post';
import { UserProfileDTO } from 'src/app/models/user-profile-dto';
import { AppServiceService } from 'src/app/services/app-service.service';

@Component({
  selector: 'app-posts',
  templateUrl: './posts.component.html',
  styleUrls: ['./posts.component.css']
})
export class PostsComponent implements OnInit, OnDestroy {
  
  private authenticatedUser: AuthenticatedUser;
  private authenticationSubscription: Subscription | undefined;

  private postsPage: number;
  private readonly pageSize: number;
  private posts: Array<Post>;

  private loadButtonDisabled: boolean;
  private message: string | undefined;


  constructor(private service: AppServiceService) {
    this.authenticatedUser = this.service.getAuthentication();
    this.postsPage = Math.floor(this.service.getPosts().length / 6);
    this.pageSize = 6;
    this.posts = this.service.getPosts();
    this.loadButtonDisabled = false;
  }


  ngOnInit(): void {
    this.authenticationSubscription = 
      this.service.getAuthenticationObservable()
        .subscribe(newAuthObject => this.authenticatedUser = newAuthObject);
    
    if(this.isUserAuthenticated()) {
      this.service.fetchPosts(this.postsPage, this.pageSize);
    }
  }

  ngOnDestroy(): void {
    this.authenticationSubscription?.unsubscribe();
  }


  public isUserAuthenticated(): boolean {
    return this.authenticatedUser.hasToken();
  }

  public getPosts(): Array<Post> {

    if(this.service.getPosts().length === 0) {
      this.postsPage = 0;
      this.service.fetchPosts(this.postsPage, this.pageSize);
    }

    return this.service.getPosts();
  }

  public isLoadButtonDisabled(): boolean {
    return this.loadButtonDisabled;
  }

  public getMessage(): string | undefined {
    return this.message;
  }

  public async loadMorePosts() {
    
    this.message = undefined;
    this.loadButtonDisabled = true;
    this.postsPage++;

    let response = await this.service.fetchPosts(this.postsPage, this.pageSize);

    if(response.executedSuccessfully) {
      // html element formatting
    }
    else {
      // html element formatting
    }
    this.message = response.message;
    this.loadButtonDisabled = false;
  }

  public getAuthorProfile(email: string): UserProfileDTO {
    return this.service.getProfileByEmail(email);
  }

}
