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
  private registrationMessage: string;
  private loginRequestError: boolean;
  private registrationRequestError: boolean;
  private loginButtonDisabled: boolean;
  private registrationButtonDisabled: boolean;

  private timeOutId: ReturnType<typeof setTimeout> | undefined;


  constructor(private service: AppServiceService, private elementRef: ElementRef) {
    this.authenticatedUser = this.service.getAuthentication();
    this.defaultLoginLabelText = "TO ACCESS BLOG POSTS OR WRITE YOURS, PLEASE LOG IN:";
    this.loginLabelText = this.defaultLoginLabelText;
    this.registrationMessage = '';
    this.loginRequestError = false;
    this.registrationRequestError = false;
    this.loginButtonDisabled = false;
    this.registrationButtonDisabled = false;
   }

  
  ngOnInit(): void {
    this.authenticationSubscription = 
      this.service.getAuthenticationObservable()
        .subscribe(newAuthObject => this.authenticatedUser = newAuthObject);
  }

  ngOnDestroy(): void {
    this.authenticationSubscription?.unsubscribe();
  }
  

  public async login(form: NgForm) {
    
    this.loginButtonDisabled = true;
    clearTimeout(this.timeOutId);

    let authRequest = new AuthRequest(form.value.email, form.value.password);
    let response = await this.service.authenticateUser(authRequest);
    
    this.loginButtonDisabled = false;

    if(response.executedSuccessfully === false) {

      this.loginLabelText = response.message;
      this.loginRequestError = true;
      
      this.timeOutId = setTimeout(() => {
          this.loginLabelText = this.defaultLoginLabelText;
          this.loginRequestError = false;
        }, 
        5000
      );
    }
  }

  public logoutUser() {
    this.loginLabelText = this.defaultLoginLabelText;
    this.service.clearAuthentication();
  }

  public async register(form: NgForm) {

    this.registrationButtonDisabled = true;
    clearTimeout(this.timeOutId);

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
      this.registrationButtonDisabled = false;

      if(response.executedSuccessfully) {
        this.timeOutId = setTimeout(() => {
            this.closeRegistrationDialog();
          }, 
          3000  
        );
      }
      else {
        this.registrationRequestError = true;

        this.timeOutId = setTimeout(() => {
            this.registrationMessage = '';
            this.registrationRequestError = false;
          }, 
          8000
        );
      }
    }
    else {
      this.registrationMessage = "The re-entered password does not match. \nPlease ensure you enter the same password in the two fields.";
      this.registrationRequestError = true;

        this.timeOutId = setTimeout(() => {
            this.registrationMessage = '';
            this.registrationRequestError = false;
          }, 
          8000
        );
    }
  }

  public getSubjectId(): string | undefined {
    return this.authenticatedUser.subjectId;
  }

  public getLoginLabelText(): string {
    return this.loginLabelText;
  }

  public getEmailToRegister(): string {
    return (this.emailToRegister) ? this.emailToRegister : '';
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

  public getRegistrationMessage(): string {
    return this.registrationMessage;
  }

  public loginRequestErrorHappened(): boolean {
    return this.loginRequestError;
  }

  public registrationRequestErrorHappened(): boolean {
    return this.registrationRequestError;
  }

  public isLoginButtonDisabled(): boolean {
    return this.loginButtonDisabled;
  }

  public isRegistrationButtonDisabled(): boolean {
    return this.registrationButtonDisabled;
  }

}
