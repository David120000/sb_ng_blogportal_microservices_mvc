import { Roles } from './role-enum';
import { User } from './user';

describe('User', () => {
  it('should create an instance', () => {

    let email = "mytestmail@host.com";
    let password = "foo";
    let role = Roles.USER;
    let accountEnabled = false;
    let firstName = "John";
    let lastName = "Doe";
    let about = "My description.";

    expect(new User(email, password, role, accountEnabled, firstName, lastName, about)).toBeTruthy();
  });
});
