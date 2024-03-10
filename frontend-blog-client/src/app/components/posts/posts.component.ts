import { Component, DoCheck, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthenticatedUser } from 'src/app/models/authenticated-user';
import { CompletionStatusInformation } from 'src/app/models/completion-status-information';
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

  private filterResultsByAuthor: string | null | undefined;
  private onlyListOwnPosts: boolean;

  private postsPage: number;
  private readonly pageSize: number;
  private firstFetchInProgress: boolean;
  private preventAutomatedFetch: boolean;

  private loadButtonDisabled: boolean;
  private message: string | undefined;


  constructor(private service: AppServiceService, private route: ActivatedRoute) {
    
    this.authenticatedUser = this.service.getAuthentication();
    
    this.filterResultsByAuthor = undefined;
    this.onlyListOwnPosts = false;
    
    this.pageSize = 4;
    this.postsPage = Math.floor(this.service.getPosts().length / this.pageSize); 
    
    this.firstFetchInProgress = false;
    this.preventAutomatedFetch = false;
    
    this.loadButtonDisabled = false;
  }


  ngOnInit(): void {
    this.authenticationSubscription = 
      this.service.getAuthenticationObservable()
        .subscribe(newAuthObject => this.authenticatedUser = newAuthObject);
  }

  ngOnDestroy(): void {
    this.authenticationSubscription?.unsubscribe();
  }


  public isUserAuthenticated(): boolean {
    return this.authenticatedUser.hasToken();
  }

  public getPosts(): Array<Post> {

    this.handleRouteChanges();

    if(this.service.getPosts().length === 0 && this.firstFetchInProgress === false && !this.preventAutomatedFetch) {
      
      this.firstFetchInProgress = true;
      this.fetchPostsNextPage()
        .then((response) => {

          if(!response.executedSuccessfully) {     
            this.message = response.message;
            this.preventAutomatedFetch = true;
          }
        })
        .finally(() => this.firstFetchInProgress = false);
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

    let response = await this.fetchPostsNextPage();

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

  private async fetchPostsNextPage(): Promise<CompletionStatusInformation> {
    
    return (this.onlyListOwnPosts) ?
      this.service.fetchPosts(this.postsPage, this.pageSize, this.authenticatedUser.subjectId, this.onlyListOwnPosts) :
        (this.filterResultsByAuthor !== null) ? this.service.fetchPosts(this.postsPage, this.pageSize, this.filterResultsByAuthor) : 
          this.service.fetchPosts(this.postsPage, this.pageSize);
  }

  private handleRouteChanges() {

    let ownPostsUrlMatch = (this.route.snapshot.url[1] !== undefined && this.route.snapshot.url[1].toString() === "posts-by-me");
    let routeAuthorParam = this.route.snapshot.paramMap.get('author');

    if(ownPostsUrlMatch && ownPostsUrlMatch !== this.onlyListOwnPosts) {
      this.service.clearPostsCache();
      this.postsPage = 0;  
    }
    else if(routeAuthorParam !== this.filterResultsByAuthor) {
      this.service.clearPostsCache();
      this.postsPage = 0;
      this.preventAutomatedFetch = false;
      this.filterResultsByAuthor = routeAuthorParam;
    }
    this.onlyListOwnPosts = ownPostsUrlMatch;
  }

}
