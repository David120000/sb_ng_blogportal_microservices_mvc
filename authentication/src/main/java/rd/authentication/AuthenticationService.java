package rd.authentication;

import java.util.NoSuchElementException;

import javax.security.auth.login.CredentialException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    
    private final UserIdentityFeignClient userClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtility jwtUtil;


    public AuthToken authenticateUser(AuthRequest userRequest) throws NoSuchElementException, CredentialException {

        if(userRequest.getEmail() == null) 
            throw new IllegalArgumentException("Email field is required. It cannot be empty (null).");
        
        if(userRequest.getPassword() == null) 
            throw new IllegalArgumentException("Password field is required. It cannot be empty (null).");

        User user = userClient
            .getUser(userRequest.getEmail().toLowerCase())
            .getBody();

        if(!user.isEnabled()) 
            throw new CredentialException("Account is disabled.");

        if(!passwordEncoder.matches(userRequest.getPassword(), user.getPassword())) 
            throw new CredentialException("Invalid password.");

        var response = AuthToken.builder()
            .jwt(jwtUtil.generateToken(user.getEmail()))
            .build();

        return response;
    }

    public UserAuthorization authorizeUser(AuthToken token) throws IllegalArgumentException {

        if(token.getJwt() == null) throw new IllegalArgumentException("JWT is required. It cannot be empty (null).");

        boolean authenticated = jwtUtil.validateToken(token.getJwt());
        String subject = jwtUtil.extractUserEmail(token.getJwt());
        String role = userClient
            .getUser(subject)
            .getBody()
            .getRole();

        var authorization = UserAuthorization.builder()
            .subjectId(subject)
            .authenticated(authenticated)
            .role(role)
            .build();

        return authorization;
    }


}
