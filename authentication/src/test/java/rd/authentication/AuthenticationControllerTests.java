package rd.authentication;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.security.auth.login.CredentialException;

import feign.FeignException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
public class AuthenticationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    private User user1;
    private User user2;


    @BeforeEach
    void beforeEach() {

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
    }

    @Test
    void testAuthenticateUser_validUserShouldBeAuthenticated() throws Exception {

        when(authenticationService.authenticateUser(
                argThat((authRequest) -> 
                    (authRequest.getEmail().equals(user1.getEmail()) && authRequest.getPassword().equals(user1.getPassword()))
                )
            ))
            .thenReturn(new AuthToken("dummyJwtHeader.dummyJwtPayload.dummyJwtSignature"));

        mockMvc.perform(
                post("/api/authenticate")
                    .accept(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"batman@waynecorp.com\",\"password\":\"qweR42!\"}")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.jwt").exists())
            .andExpect(jsonPath("$.jwt", Matchers.matchesRegex("(^[\\w-]*\\.[\\w-]*\\.[\\w-]*$)")));
        
        verify(authenticationService, times(1)).authenticateUser(any(AuthRequest.class));
    }

    @Test
    void testAuthenticateUser_notRegisteredUserShouldReturnBadRequestError() throws Exception {

        when(authenticationService.authenticateUser(
                argThat((authRequest) -> 
                    authRequest.getEmail().equals(user2.getEmail())
                )
            ))
            .thenThrow(FeignException.class);

        mockMvc.perform(
                post("/api/authenticate")
                    .accept(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"clark_k@dailyplanet.net\",\"password\":\"qweR42!\"}")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().is4xxClientError());
            // the ControllerAdvice catches the Exception and returns 400 BAD REQUEST instead of a 5xx server error
        
        verify(authenticationService, times(1)).authenticateUser(any(AuthRequest.class));
    }

    @Test
    void testAuthenticateUser_wrongUserPasswordShouldReturnBadRequestError() throws Exception {

        when(authenticationService.authenticateUser(
                argThat((authRequest) -> 
                    authRequest.getPassword().equals(user2.getPassword()) == false
                )
            ))
            .thenThrow(new CredentialException("Invalid password."));

        mockMvc.perform(
                post("/api/authenticate")
                    .accept(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"clark_k@dailyplanet.net\",\"password\":\"myMistypedPassword\"}")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().is4xxClientError());
            // the ControllerAdvice catches the Exception and returns 400 BAD REQUEST instead of a 5xx server error
        
        verify(authenticationService, times(1)).authenticateUser(any(AuthRequest.class));
    }

    @Test
    void testAuthenticateUser_disabledUserShouldReturnBadRequestError() throws Exception {

        when(authenticationService.authenticateUser(
                argThat((authRequest) -> 
                    (authRequest.getEmail().equals(user2.getEmail()) && !user2.isEnabled())
                )
            ))
            .thenThrow(new CredentialException("Account is disabled."));

        mockMvc.perform(
                post("/api/authenticate")
                    .accept(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"clark_k@dailyplanet.net\",\"password\":\"qweR42!\"}")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().is4xxClientError());
            // the ControllerAdvice catches the Exception and returns 400 BAD REQUEST instead of a 5xx server error
        
        verify(authenticationService, times(1)).authenticateUser(any(AuthRequest.class));
    }

    @Test
    void testAuthenticateUser_requestWithNullEmailShouldReturnBadRequestError() throws Exception {

        when(authenticationService.authenticateUser(
                argThat((authRequest) -> 
                    authRequest.getEmail() == null
                )
            ))
            .thenThrow(new IllegalArgumentException("Email field is required. It cannot be empty (null)."));

        mockMvc.perform(
                post("/api/authenticate")
                    .accept(MediaType.APPLICATION_JSON)
                    .content("{\"password\":\"qweR42!\"}")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().is4xxClientError());
            // the ControllerAdvice catches the Exception and returns 400 BAD REQUEST instead of a 5xx server error
        
        verify(authenticationService, times(1)).authenticateUser(any(AuthRequest.class));
    }

    @Test
    void testAuthenticateUser_requestWithNullPasswordShouldReturnBadRequestError() throws Exception {

        when(authenticationService.authenticateUser(
                argThat((authRequest) -> 
                    authRequest.getPassword() == null
                )
            ))
            .thenThrow(new IllegalArgumentException("Password field is required. It cannot be empty (null)."));

        mockMvc.perform(
                post("/api/authenticate")
                    .accept(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"clark_k@dailyplanet.net\"}")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().is4xxClientError());
            // the ControllerAdvice catches the Exception and returns 400 BAD REQUEST instead of a 5xx server error
        
        verify(authenticationService, times(1)).authenticateUser(any(AuthRequest.class));
    }

    @Test
    void testAuthorizeUser_validUserShouldBeAuthorized() throws Exception {

        when(authenticationService.authorizeUser(
                argThat((authToken) -> 
                    authToken.getJwt().matches("(^[\\w-]*\\.[\\w-]*\\.[\\w-]*$)")
                )
            ))
            .thenReturn(
                UserAuthorization.builder()
                    .subjectId(user1.getEmail())    
                    .authenticated(true)
                    .role(user1.getRole())
                    .build()
            );

        mockMvc.perform(
                post("/api/authorize")
                    .accept(MediaType.APPLICATION_JSON)
                    .content("{\"jwt\":\"dummyJwtHeader.dummyJwtPayload.dummyJwtSignature\"}")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.subjectId").value("batman@waynecorp.com"))
            .andExpect(jsonPath("$.authenticated").value(true))
            .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(authenticationService, times(1)).authorizeUser(any(AuthToken.class));
    }

    @Test
    void testAuthorizeUser_invalidTokenShouldReturnBadRequestError() throws Exception {

        when(authenticationService.authorizeUser(
                argThat((authToken) -> 
                    authToken.getJwt().equals("dummyJwtHeader.dummyJwtPayload.dummyJwtSignature")
                )
            ))
            .thenThrow(new IllegalArgumentException("Could not verify JWT token."));

        mockMvc.perform(
                post("/api/authorize")
                    .accept(MediaType.APPLICATION_JSON)
                    .content("{\"jwt\":\"dummyJwtHeader.dummyJwtPayload.dummyJwtSignature\"}")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().is4xxClientError());
            // the ControllerAdvice catches the Exception and returns 400 BAD REQUEST instead of a 5xx server error

        verify(authenticationService, times(1)).authorizeUser(any(AuthToken.class));
    }

    @Test
    void testAuthorizeUser_nullJwtShouldReturnBadRequestError() throws Exception {

        when(authenticationService.authorizeUser(
                argThat((authToken) -> 
                    authToken.getJwt() == null
                )
            ))
            .thenThrow(new IllegalArgumentException("JWT is required. It cannot be empty (null)."));

        mockMvc.perform(
                post("/api/authorize")
                    .accept(MediaType.APPLICATION_JSON)
                    .content("{}")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().is4xxClientError());
            // the ControllerAdvice catches the Exception and returns 400 BAD REQUEST instead of a 5xx server error

        verify(authenticationService, times(1)).authorizeUser(any(AuthToken.class));
    }
}
