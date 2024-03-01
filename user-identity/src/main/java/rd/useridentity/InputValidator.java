package rd.useridentity;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class InputValidator {

    private final Pattern emailPattern;
    private final Pattern passwordPattern;
    private final List<String> enabledRoles;


    public InputValidator() {

        String emailRegex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        this.emailPattern = Pattern.compile(emailRegex);

        String passwordRegex = "^(?=.*[A-Z])(?=.*[\\Q_-!@#$&*\\\\E])(?=.*[0-9])(?=.*[a-z]).{6,}$";
        this.passwordPattern = Pattern.compile(passwordRegex);

        this.enabledRoles = Arrays.asList("USER", "ADMIN");
    }
    

    public boolean validateUser(User user) throws IllegalArgumentException {

        Matcher emailMatcher = this.emailPattern.matcher(user.getEmail());
        boolean validEmail = emailMatcher.matches();
        if(!validEmail) throw new IllegalArgumentException("The given email address (" + user.getEmail() + ") is invalid.");

        Matcher passwordMatcher = this.passwordPattern.matcher(user.getPassword());
        boolean validPassword = passwordMatcher.matches();
        if(!validPassword) throw new IllegalArgumentException(
            "The given password is invalid. " +
            "The password should be at least 6 characters long and contain all of the following: " +
            "lower case letter, upper case letter, number, any special character: _-!@#$&*");

        boolean enabledRole = this.enabledRoles.contains(user.getRole());
        if(!enabledRole) throw new IllegalArgumentException("The given user role (" + user.getRole() + ") is invalid.");

        return (validEmail && validPassword && enabledRole);
    }
}
