import { Roles } from "./role-enum";

export class User {

    email: string;
    password: string;
    role: Roles;
    accountEnabled: boolean;
    firstName: string;
    lastName: string;
    about: string;

    constructor(email: string, password: string, role: Roles, accountEnabled: boolean, firstName: string, lastName: string, about: string) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.accountEnabled = accountEnabled;
        this.firstName = firstName;
        this.lastName = lastName;
        this.about = about;
    }
    
}
