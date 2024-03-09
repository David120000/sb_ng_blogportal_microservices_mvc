import { Injectable } from '@angular/core';
import { UserProfileDTO } from '../models/user-profile-dto';
import { Post } from '../models/post';

@Injectable({
  providedIn: 'root'
})
export class PostsCacheService {

  private posts: Array<Post>;
  private postsMap: Map<string, Post>;
  private authors: Map<string, UserProfileDTO>;

  constructor() {
    this.posts = new Array<Post>();
    this.postsMap = new Map<string, Post>();
    this.authors = new Map<string, UserProfileDTO>();
  }


  public addToPostsCache(post: Post) {
    
    if(post.id !== undefined && !this.postsMap.has(post.id)) {
      this.postsMap.set(post.id, post);
      this.posts.push(post);
    }
  }

  public addFirstToPostsCache(post: Post) {
    
    if(post.id !== undefined && !this.postsMap.has(post.id)) {
      this.postsMap.set(post.id, post);
      this.posts.unshift(post);
    } 
  }

  public getPosts(): Array<Post> {
    return this.posts;
  }

  public clearPostsCache() {
    this.posts = new Array<Post>();
    this.postsMap = new Map<string, Post>();    
  }

  public addToAuthorsCache(profile: UserProfileDTO) {
    
    this.authors.set(
      (profile.email) ? profile.email : "nokey", 
      profile
    );
  }

  public getAuthor(email: string): UserProfileDTO | undefined {
    return this.authors.get(email);
  }

  public hasAuthor(email: string): boolean {
    return this.authors.has(email);
  }
}
