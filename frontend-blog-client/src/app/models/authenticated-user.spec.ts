import { AuthenticatedUser } from './authenticated-user';

describe('AuthenticatedUser', () => {
  it('should create an instance', () => {
    expect(new AuthenticatedUser(undefined, undefined)).toBeTruthy();
  });
});
