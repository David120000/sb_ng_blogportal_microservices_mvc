import { UserRegistrationDTO } from './user-registration-dto';

describe('UserRegistrationDTO', () => {
  it('should create an instance', () => {
    expect(new UserRegistrationDTO('email', 'password', 'firstName', 'lastName', 'about')).toBeTruthy();
  });
});
