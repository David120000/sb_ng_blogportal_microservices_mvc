import { Injectable, OnDestroy } from '@angular/core';
import { AuthenticatedUser } from '../models/authenticated-user';
import { Observable, Subject, Subscription } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationObjectService implements OnDestroy {

  private authenticatedUser: AuthenticatedUser;
  private readonly authObjectSource: Subject<AuthenticatedUser>;
  private readonly authObjectObservable$: Observable<AuthenticatedUser>;

  private authObjectSubscription: Subscription;

  constructor() { 
    
    this.authenticatedUser = new AuthenticatedUser(undefined, undefined);
    this.authObjectSource = new Subject<AuthenticatedUser>;
    this.authObjectObservable$ = this.authObjectSource.asObservable();

    this.authObjectSubscription = this.authObjectObservable$.subscribe(
      newAuthenticatedUser => this.authenticatedUser = newAuthenticatedUser
    );
  }

  ngOnDestroy(): void {
    this.authObjectSubscription.unsubscribe();
  }


  public setAuthentication(authentication: AuthenticatedUser) {
    this.authObjectSource.next(authentication);
  }

  public clearAuthentication() {
    this.authObjectSource.next(new AuthenticatedUser(undefined, undefined));
  }

  public getAuthentication(): AuthenticatedUser {
    return this.authenticatedUser;
  }

  public getAuthenticationObservable(): Observable<AuthenticatedUser> {
    return this.authObjectObservable$;
  }

}
