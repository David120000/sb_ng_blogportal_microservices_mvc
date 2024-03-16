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

  public deletePostFromCache(post: Post) {

    if(post.id !== undefined && this.postsMap.has(post.id)) {
      let index = this.posts.indexOf(post);
      if(index > -1) {
        this.posts.splice(index, 1);
        this.postsMap.delete(post.id);
      }
    }
  }

  public updatePostInCache(post: Post) {
    
    if(post.id !== undefined && this.postsMap.has(post.id)) {
      let index = -1;

      for(let searchIndex = 0; searchIndex < this.posts.length; searchIndex++) {
        
        if(this.posts.at(searchIndex)?.id === post.id) {
          index = searchIndex;
          break;
        }
      }

      if(index > -1) 
        this.posts.splice(index, 1, post);
    }
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
