import { Component, OnDestroy, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Subscription } from 'rxjs';
import { AuthenticatedUser } from 'src/app/models/authenticated-user';
import { NewPostDTO } from 'src/app/models/new-post-dto';
import { AppServiceService } from 'src/app/services/app-service.service';

@Component({
  selector: 'app-newpost',
  templateUrl: './newpost.component.html',
  styleUrls: ['./newpost.component.css']
})
export class NewPostComponent implements OnInit, OnDestroy{

  private authenticatedUser: AuthenticatedUser;
  private authenticationSubscription: Subscription | undefined;

  private message: string | undefined;
  private submitButtonDisabled: boolean;
  public publishCheckerDefault: boolean;

  private requestError: boolean;

  constructor(private service: AppServiceService) {
    this.authenticatedUser = this.service.getAuthentication();
    this.submitButtonDisabled = false;
    this.publishCheckerDefault = true;
    this.requestError = false;
  }


  ngOnInit(): void {
    this.authenticationSubscription = 
      this.service.getAuthenticationObservable()
        .subscribe(newAuthObject => this.authenticatedUser = newAuthObject);
  }

  ngOnDestroy(): void {
    this.authenticationSubscription?.unsubscribe();
  }

  
  public async newPost(form: NgForm) {
    
    this.submitButtonDisabled = true;
    this.requestError = false;
    this.message = undefined;

    let post = new NewPostDTO(
      (this.authenticatedUser.subjectId) ? this.authenticatedUser.subjectId : "anonymus",
      form.value.content,
      form.value.published
    );
    
    let response = await this.service.newPost(post);
    
    this.requestError = response.executedSuccessfully;
    this.message = response.message;
    
    this.submitButtonDisabled = false;
    this.publishCheckerDefault = true;
  }

  public isUserAuthenticated(): boolean {
    return this.authenticatedUser.hasToken();
  }

  public getMessage(): string | undefined {
    return this.message;
  }

  public isSubmitButtonDisabled(): boolean {
    return this.submitButtonDisabled;
  }

  public noRequestErrorHappened(): boolean {
    return this.requestError;
  }

}
