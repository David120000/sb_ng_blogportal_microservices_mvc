package rd.useridentity;

import java.util.NoSuchElementException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import rd.useridentity.model.User;
import rd.useridentity.model.UserProfileDataDTO;
import rd.useridentity.model.UserSecurityDTO;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final InputValidator validator;
    private final UserToDTOMapper mapper;
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;


    public UserProfileDataDTO getUserProfileData(String email) {

        var user = repository.findById(email)
            .orElseThrow();
        
        return mapper.buildProfileDataDTO(user);
    }

    public UserSecurityDTO getUserSecurityProfile(String email) {

        var user = repository.findById(email)
            .orElseThrow();

        return mapper.buildSecurityDTO(user);
    }

    public void registerUser(User user) {

        this.validateUserObjectArguments(user);

        if(!repository.existsById(user.getEmail().toLowerCase())) {

            if(validator.validateUser(user)) {

                user.setEmail(user.getEmail().toLowerCase());
                user.setPassword(
                    passwordEncoder.encode(user.getPassword())
                );
                
                repository.save(user);
            } 
        }
        else {
            throw new IllegalArgumentException("The email address " + user.getEmail() + " already registered.");
        }       
    }

    public boolean checkIfUserExistsInDb(String email) {
        return repository.existsById(email);
    }

    public void updateUserData(User user) {

        this.validateUserObjectArguments(user);

        if(repository.existsById(user.getEmail().toLowerCase())) {

            if(validator.validateUser(user)) {
            
                user.setEmail(user.getEmail().toLowerCase());
                user.setPassword(
                    passwordEncoder.encode(user.getPassword())
                );
                
                repository.save(user);
            } 
        }
        else {
            throw new NoSuchElementException("Could not find account with " + user.getEmail() + " address.");
        }

    }

    public void deleteUserById(String email) {
        repository.deleteById(email);
    }

    private void validateUserObjectArguments(User user) {

        if(user.getEmail() == null) throw new IllegalArgumentException("Email field is required. It cannot be empty (null).");
        if(user.getPassword() == null) throw new IllegalArgumentException("Password field is required. It cannot be empty (null).");
        if(user.getRole() == null) throw new IllegalArgumentException("Role field is required. It cannot be empty (null).");
        if(user.getAccountEnabled() == null) throw new IllegalArgumentException("Account Enabled field is required. It cannot be empty (null).");
        if(user.getFirstName() == null) throw new IllegalArgumentException("First Name field is required. It cannot be empty (null).");
        if(user.getLastName() == null) throw new IllegalArgumentException("Last Name field is required. It cannot be empty (null).");
    }

}
