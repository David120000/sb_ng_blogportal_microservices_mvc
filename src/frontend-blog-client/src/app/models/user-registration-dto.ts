export class UserRegistrationDTO {

    email: string;
    password: string;
    firstName: string;
    lastName: string;
    about: string;

    constructor(email: string, password: string, firstName: string, lastName: string, about: string) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.about = about;
    }
}
