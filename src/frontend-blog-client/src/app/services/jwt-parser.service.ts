import { Injectable } from '@angular/core';
import { JwtHelperService } from '@auth0/angular-jwt';
import { AuthToken } from '../models/auth-token';

@Injectable({
  providedIn: 'root'
})
export class JwtParserService {

  private jwtHelperService: JwtHelperService;

  constructor() {
    this.jwtHelperService = new JwtHelperService();
   }


  public getSubjectId(securityToken: AuthToken): string | undefined {

    let subject = undefined;
    if(securityToken.jwt != undefined) {
      subject = this.jwtHelperService.decodeToken(securityToken.jwt).sub;
    }

    return subject;
  }

  public isTokenExpired(securityToken: AuthToken): boolean {

    let expired = true;
    if(securityToken.jwt != undefined) {
      expired = this.jwtHelperService.isTokenExpired(securityToken.jwt);
    }

    return expired;
  }
  
}
