import { AuthRequest } from './auth-request';

describe('AuthRequest', () => {

  let email = "mytestmail@host.com";
  let password = "foo";

  it('should create an instance', () => {
    expect(new AuthRequest(email, password)).toBeTruthy();
  });
});
