package rd.useridentity;

import java.util.NoSuchElementException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

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

    public void updateUserData(User user) {

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

}