package rd.authentication;

import java.util.NoSuchElementException;

import javax.security.auth.login.CredentialException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthenticationController {
    
    private final AuthenticationService service;


    @PostMapping("/authenticate")
    public ResponseEntity<AuthToken> authenticateUser(
            @RequestBody AuthRequest request
        ) throws CredentialException, NoSuchElementException {
        
        return ResponseEntity.ok(service.authenticateUser(request));
    }

    @PostMapping("/authorize")
    public ResponseEntity<UserAuthorization> authorizeUser(
            @RequestBody AuthToken token
        ) throws IllegalArgumentException {
        
        return ResponseEntity.ok(service.authorizeUser(token));
    }

}
