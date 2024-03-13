import { Component, DoCheck, ElementRef, OnDestroy, OnInit, Renderer2 } from '@angular/core';
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
  private fetchError: boolean;
  private fetchMessageVisible: boolean;
  private timeOutID: ReturnType<typeof setTimeout> | undefined;

  private lineColors: Array<string>;


  constructor(
    private service: AppServiceService, 
    private route: ActivatedRoute,
    private elementRef: ElementRef,
    private renderer: Renderer2
    ) {
    
    this.authenticatedUser = this.service.getAuthentication();
    
    this.filterResultsByAuthor = undefined;
    this.onlyListOwnPosts = false;
    
    this.pageSize = 4;
    this.postsPage = Math.floor(this.service.getPosts().length / this.pageSize); 
    
    this.firstFetchInProgress = false;
    this.preventAutomatedFetch = false;
    
    this.loadButtonDisabled = false;
    this.fetchError = false;
    this.fetchMessageVisible = true;

    this.lineColors = [
      '#2196F3', '#32c787', '#00BCD4', '#ff5652',
      '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
    ];
  }


  ngOnInit(): void {
    this.authenticationSubscription = 
      this.service.getAuthenticationObservable()
        .subscribe(newAuthObject => this.authenticatedUser = newAuthObject);
  }

  ngOnDestroy(): void {
    this.authenticationSubscription?.unsubscribe();
  }

  public getPosts(): Array<Post> {

    this.handleRouteChanges();

    if(this.service.getPosts().length === 0 && this.firstFetchInProgress === false && !this.preventAutomatedFetch) {
      
      clearTimeout(this.timeOutID);
      this.firstFetchInProgress = true;
      
      this.fetchPostsNextPage()
        .then((response) => {

          if(!response.executedSuccessfully) {     
            this.message = response.message;
            this.fetchError = true;
            this.preventAutomatedFetch = true;
          }
        })
        .finally(() => this.firstFetchInProgress = false);
    }

    return this.service.getPosts();
  }

  public async loadMorePosts() {
    
    clearTimeout(this.timeOutID);
    this.message = undefined;
    this.fetchError = false;
    this.loadButtonDisabled = true;

    this.postsPage++;

    let response = await this.fetchPostsNextPage();

    this.fetchError = response.executedSuccessfully;
    this.message = response.message;
    this.fetchMessageVisible = true;
    this.loadButtonDisabled = false;

    this.timeOutID = setTimeout(() => {
        this.fetchMessageVisible = false;
      }, 
      3500
    ); // I will hide the message after the timeout, but I won't change the text back to undefined in an other timeout to save one unnecessary re-render.
  }

  public getAuthorProfile(email: string): UserProfileDTO {
    return this.service.getProfileByEmail(email);
  }

  public getAuthorColor(stringToHash: string): string | undefined {

    let hash = 0;
    for (let i = 0; i < stringToHash.length; i++) {
        hash = hash + stringToHash.charCodeAt(i);
    }

    let colorIndex = Math.abs(hash % this.lineColors.length);

    return this.lineColors.at(colorIndex) + '6e';
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

  public isUserAuthenticated(): boolean {
    return this.authenticatedUser.hasToken();
  }

  public isLoadButtonDisabled(): boolean {
    return this.loadButtonDisabled;
  }

  public getMessage(): string | undefined {
    return this.message;
  }

  public fetchErrorHappened(): boolean {
    return this.fetchError;
  }

  public isFetchMessageVisible(): boolean {
    return this.fetchMessageVisible;
  }

}
