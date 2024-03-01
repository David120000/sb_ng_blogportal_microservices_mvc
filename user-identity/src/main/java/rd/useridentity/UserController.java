package rd.useridentity;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService service;


    @GetMapping("/user/security/{user-email}")
    public ResponseEntity<UserSecurityDTO> getUserSecurityProfile(@PathVariable(name="user-email") String email) {

        var userSecurityProfile = service.getUserSecurityProfile(email);
        return ResponseEntity.ok(userSecurityProfile);
    }

    @GetMapping("/user/profile/{user-email}")
    public ResponseEntity<UserProfileDataDTO> getUserProfileData(@PathVariable(name="user-email") String email) {
        
        var userProfileData = service.getUserProfileData(email);
        return ResponseEntity.ok(userProfileData);
    }

    @PostMapping("/user/new")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerUser(@RequestBody User user) {
        
        service.registerUser(user);
    }

    @PutMapping("/user/update")
    public ResponseEntity<Void> updateUserData(@RequestBody User user) {
        
        service.updateUserData(user);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/user/delete/{user-email}")
    public ResponseEntity<Void> deleteUser(@PathVariable(name="user-email") String email) {

        service.deleteUserById(email);
        return ResponseEntity.accepted().build();
    }
}
