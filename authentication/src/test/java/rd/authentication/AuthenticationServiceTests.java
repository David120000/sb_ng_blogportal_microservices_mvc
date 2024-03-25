package rd.authentication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import rd.authentication.model.AuthRequest;
import rd.authentication.model.AuthToken;
import rd.authentication.model.User;
import rd.authentication.model.UserAuthorization;

import java.util.NoSuchElementException;
import javax.security.auth.login.CredentialException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AuthenticationServiceTests {

    @InjectMocks
    private AuthenticationService authenticationService;

    private User user1;
    private User user2;
    private User user3;

    @Mock
    private UserIdentityFeignClient userClient;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtility jwtUtility;


    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.openMocks(this);

        user1 = User.builder()
            .email("batman@waynecorp.com")
            .password("qweR42!")
            .role("ADMIN")
            .enabled(true)
            .build();

        user2 = User.builder()
            .email("clark_k@dailyplanet.net")
            .password("qweR42!")
            .role("USER")
            .enabled(false)
            .build();
        
        user3 = User.builder().build();
    }


    @Test
    void testAuthenticateUser_validUserShouldGetAuthenticated() throws CredentialException, NoSuchElementException {

        when(userClient.getUser("batman@waynecorp.com"))
            .thenReturn(ResponseEntity.ok(user1));

        when(passwordEncoder.matches("qweR42!", user1.getPassword()))
            .thenReturn(true);

        when(jwtUtility.generateToken("batman@waynecorp.com"))
            .thenReturn("dummyJwtHeader.dummyJwtPayload.dummyJwtSignature");

        AuthRequest request = AuthRequest.builder()
            .email(user1.getEmail())
            .password(user1.getPassword())
            .build();
        
        AuthToken token = authenticationService.authenticateUser(request);
        
        assertNotNull(token);
        assertNotNull(token.getJwt());
        assertTrue(token.getJwt().matches("(^[\\w-]*\\.[\\w-]*\\.[\\w-]*$)"));

        verify(userClient, times(1)).getUser(anyString());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verify(jwtUtility, times(1)).generateToken(anyString());
    }

    @Test
    void testAuthenticateUser_disabledUserShouldThrowError() {

        when(userClient.getUser("clark_k@dailyplanet.net"))
            .thenReturn(ResponseEntity.ok(user2));

        AuthRequest request = AuthRequest.builder()
            .email(user2.getEmail())
            .password(user2.getPassword())
            .build();
        
        Throwable throwable =
            assertThrows(CredentialException.class, () -> authenticationService.authenticateUser(request));

        assertEquals("Account is disabled.", throwable.getMessage());

        verify(userClient, times(1)).getUser(anyString());
    }

    @Test
    void testAuthenticateUser_wrongPasswordShouldThrowError() {

        when(userClient.getUser("batman@waynecorp.com"))
            .thenReturn(ResponseEntity.ok(user1));

        when(passwordEncoder.matches("NotTheRightPassword", user1.getPassword()))
            .thenReturn(false);

        AuthRequest request = AuthRequest.builder()
            .email(user1.getEmail())
            .password("NotTheRightPassword")
            .build();
        
        Throwable throwable =
            assertThrows(CredentialException.class, () -> authenticationService.authenticateUser(request));

        assertEquals("Invalid password.", throwable.getMessage());

        verify(userClient, times(1)).getUser(anyString());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    }

    @Test
    void testAuthenticateUser_nullEmailShouldThrowError() {

        AuthRequest request = AuthRequest.builder()
            .email(user3.getEmail())
            .password(user3.getPassword())
            .build();
        
        Throwable throwable =
            assertThrows(IllegalArgumentException.class, () -> authenticationService.authenticateUser(request));

        assertEquals("Email field is required. It cannot be empty (null).", throwable.getMessage());
    }

    @Test
    void testAuthenticateUser_nullPasswordShouldThrowError() {

        AuthRequest request = AuthRequest.builder()
            .email(user1.getEmail())
            .password(user3.getPassword())
            .build();
        
        Throwable throwable =
            assertThrows(IllegalArgumentException.class, () -> authenticationService.authenticateUser(request));

        assertEquals("Password field is required. It cannot be empty (null).", throwable.getMessage());
    }

    @Test
    void testAuthorizeUser_validTokenShouldBeAuthorized() {

        when(jwtUtility.validateToken(
                argThat((jwt) -> jwt.matches("(^[\\w-]*\\.[\\w-]*\\.[\\w-]*$)"))
            ))
            .thenReturn(true);
        
        when(jwtUtility.extractUserEmail(
                argThat((jwt) -> jwt.matches("(^[\\w-]*\\.[\\w-]*\\.[\\w-]*$)"))
            ))
            .thenReturn("batman@waynecorp.com");

        when(userClient.getUser("batman@waynecorp.com"))
            .thenReturn(ResponseEntity.ok(user1));

        AuthToken token = AuthToken.builder()
            .jwt("dummyJwtHeader.dummyJwtPayload.dummyJwtSignature")
            .build();

        UserAuthorization authorization =
            authenticationService.authorizeUser(token);

        assertEquals(user1.getEmail(), authorization.getSubjectId());
        assertEquals(user1.getRole(), authorization.getRole());
        assertTrue(authorization.getAuthenticated());

        verify(jwtUtility, times(1)).validateToken(anyString());
        verify(jwtUtility, times(1)).extractUserEmail((anyString()));
        verify(userClient, times(1)).getUser(anyString());
    }

    @Test
    void testAuthorizeUser_invalidTokenShouldThrowError() {

        when(jwtUtility.validateToken(anyString()))
            .thenThrow(new IllegalArgumentException("Could not verify JWT token."));
        
        AuthToken token = AuthToken.builder()
            .jwt("dummyJwtHeader.dummyJwtPayload.dummyJwtSignature")
            .build();

        Throwable throwable =
            assertThrows(IllegalArgumentException.class, () -> authenticationService.authorizeUser(token));

        assertEquals("Could not verify JWT token.", throwable.getMessage());

        verify(jwtUtility, times(1)).validateToken(anyString());
    }

    @Test
    void testAuthorizeUser_nullTokenShouldThrowError() {
        
        AuthToken token = AuthToken.builder().build();

        Throwable throwable =
            assertThrows(IllegalArgumentException.class, () -> authenticationService.authorizeUser(token));

        assertEquals("JWT is required. It cannot be empty (null).", throwable.getMessage());
    }
}
