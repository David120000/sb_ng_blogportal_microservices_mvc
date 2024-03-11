import { Component, ElementRef, OnDestroy, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Subscription } from 'rxjs';
import { AuthRequest } from 'src/app/models/auth-request';
import { AuthenticatedUser } from 'src/app/models/authenticated-user';
import { UserRegistrationDTO } from 'src/app/models/user-registration-dto';
import { AppServiceService } from 'src/app/services/app-service.service';

@Component({
  selector: 'app-authentication',
  templateUrl: './authentication.component.html',
  styleUrls: ['./authentication.component.css']
})
export class AuthenticationComponent implements OnInit, OnDestroy {

  private authenticatedUser: AuthenticatedUser;
  private authenticationSubscription: Subscription | undefined;

  private readonly defaultLoginLabelText: string;
  private loginLabelText: string;
  private emailToRegister: string | undefined;
  private registrationMessage: string | undefined;


  constructor(private service: AppServiceService, private elementRef: ElementRef) {
    this.authenticatedUser = this.service.getAuthentication();
    this.defaultLoginLabelText = "TO ACCESS BLOG POSTS OR WRITE YOURS, PLEASE LOG IN:";
    this.loginLabelText = this.defaultLoginLabelText;
   }

  
  ngOnInit(): void {
    this.authenticationSubscription = 
      this.service.getAuthenticationObservable()
        .subscribe(newAuthObject => this.authenticatedUser = newAuthObject);
  }

  ngOnDestroy(): void {
    this.authenticationSubscription?.unsubscribe();
  }
  

  public getSubjectId(): string | undefined {
    return this.authenticatedUser.subjectId;
  }

  public getLoginLabelText(): string {
    return this.loginLabelText;
  }

  public async login(form: NgForm) {
    
    let authRequest = new AuthRequest(form.value.email, form.value.password);
    
    let response = await this.service.authenticateUser(authRequest);
     
    if(response.executedSuccessfully === false) {
      this.loginLabelText = response.message;
    }
  }

  public logoutUser() {
    this.loginLabelText = this.defaultLoginLabelText;
    this.service.clearAuthentication();
  }

  public getEmailToRegister(): string | undefined {
    return this.emailToRegister;
  }

  public showRegistrationDialog(emailToRegister: string | undefined) {
    this.emailToRegister = emailToRegister;
    this.elementRef.nativeElement.querySelector('#registration-dialog')
      .showModal();
  }

  public closeRegistrationDialog() {
    this.emailToRegister = undefined;
    this.elementRef.nativeElement.querySelector('#registration-dialog')
      .close();
  }

  public getRegistrationMessage(): string | undefined {
    return this.registrationMessage;
  }

  public async register(form: NgForm) {

    if(form.value.passwordfirst === form.value.passwordsecond) {

      let registrationRequest = 
        new UserRegistrationDTO(
          form.value.email,
          form.value.passwordfirst,
          form.value.firstname,
          form.value.lastname,
          (form.value.about) ? form.value.about : ''
        );

      let response = await this.service.registerUser(registrationRequest);
      this.registrationMessage = response.message;

      if(response.executedSuccessfully) {
        // html element formatting
      }
      else {
        // html element formatting
      }
    }
    else {
      this.registrationMessage = "The re-entered password does not match. Please ensure you enter the same password in the two fields.";
    }
  }


}
