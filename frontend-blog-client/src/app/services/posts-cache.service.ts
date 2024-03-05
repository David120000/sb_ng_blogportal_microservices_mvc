import { Injectable } from '@angular/core';
import { UserProfileDTO } from '../models/user-profile-dto';
import { Post } from '../models/post';

@Injectable({
  providedIn: 'root'
})
export class PostsCacheService {

  private posts: Array<Post>;
  private authors: Map<string, UserProfileDTO>;

  constructor() {
    this.posts = new Array<Post>();
    this.authors = new Map<string, UserProfileDTO>();
  }


  public addToPostsCache(post: Post) {
    
    if(!this.posts.includes(post)) {
      this.posts.push(post);
    }
  }

  public addFirstToPostsCache(post: Post) {
    
    if(this.posts.length > 0) {
      // for better performance, addFirst only checks the current first element and not the whole array with includes()
      if(this.posts.at(0)?.id != post.id) {
        this.posts.unshift(post);
      }
    }
    else {
      this.posts.unshift(post);
    } 
  }

  public getPosts(): Array<Post> {
    return this.posts;
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
