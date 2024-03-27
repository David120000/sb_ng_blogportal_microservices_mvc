package rd.useridentity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import rd.useridentity.model.User;
import rd.useridentity.model.UserProfileDataDTO;
import rd.useridentity.model.UserSecurityDTO;

import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private User user1;
    private User user2;


    @BeforeEach
    void BeforeEach() {

        user1 = User.builder()
            .email("batman@waynecorp.com")
            .password("qweR42!")
            .role("ADMIN")
            .accountEnabled(true)
            .firstName("Bruce")
            .lastName("Wayne")
            .about("When I am not driving around in Gotham, I spend most of my time in my own cave.")
            .build();

        user2 = User.builder()
            .email("clark_k@dailyplanet.net")
            .password("qweR42!")
            .role("USER")
            .accountEnabled(false)
            .firstName("Clark")
            .lastName("Kent")
            .about("Sometimes I feel like an alien here.")
            .build();
    }


    @Test
    void testCheckIfUserExists_registeredShouldReturnTrue() throws Exception {

        when(userService.checkIfUserExistsInDb("batman@waynecorp.com"))
            .thenReturn(true);

        mockMvc.perform(get("/api/user/exists/" + user1.getEmail()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }

    @Test
    void testCheckIfUserExists_notRegisteredShouldReturnFalse() throws Exception {

        when(userService.checkIfUserExistsInDb("clark_k@dailyplanet.net"))
            .thenReturn(false);

        mockMvc.perform(get("/api/user/exists/" + user2.getEmail()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string("false"));
    }

    @Test
    void testDeleteUser() throws Exception {

        doNothing().when(userService).deleteUserById("batman@waynecorp.com");

        mockMvc.perform(delete("/api/user/delete/" + user1.getEmail()))
            .andDo(print())
            .andExpect(status().isAccepted());
    }

    @Test
    void testGetUserProfileData_registeredShouldReturnData() throws Exception {

        when(userService.getUserProfileData("batman@waynecorp.com"))
            .thenReturn(
                new UserProfileDataDTO(
                    user1.getEmail(), 
                    user1.getFirstName() + " " + user1.getLastName(), 
                    user1.getAbout()
                )
            );
        
        mockMvc.perform(get("/api/user/profile/" + user1.getEmail()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.email").value("batman@waynecorp.com"))
            .andExpect(jsonPath("$.name").value("Bruce Wayne"))
            .andExpect(jsonPath("$.about").exists());
    }

    @Test
    void testGetUserProfileData_notRegisteredShouldReturnBadRequestError() throws Exception {

        when(userService.getUserProfileData("clark_k@dailyplanet.net"))
            .thenThrow(new NoSuchElementException());
        
        mockMvc.perform(get("/api/user/profile/" + user2.getEmail()))
            .andDo(print())
            .andExpect(status().is4xxClientError()); 
            // the ControllerAdvice catches the Exception and returns 400 BAD REQUEST instead of a 5xx server error
    }

    @Test
    void testGetUserSecurityProfile_registeredShouldReturnData() throws Exception {

        when(userService.getUserSecurityProfile("batman@waynecorp.com"))
            .thenReturn(
                new UserSecurityDTO(
                    user1.getEmail(),
                    user1.getPassword(), 
                    user1.getRole(), 
                    user1.getAccountEnabled()
                )
            );

        mockMvc.perform(get("/api/user/security/" + user1.getEmail()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.email").value("batman@waynecorp.com"))
            .andExpect(jsonPath("$.password").value("qweR42!"))
            .andExpect(jsonPath("$.role").value("ADMIN"))
            .andExpect(jsonPath("$.enabled").value("true"));
    }

    @Test
    void testGetUserSecurityProfile_notRegisteredShouldReturnBadRequestError() throws Exception {

        when(userService.getUserSecurityProfile("clark_k@dailyplanet.net"))
            .thenThrow(new NoSuchElementException());
        
        mockMvc.perform(get("/api/user/security/" + user2.getEmail()))
            .andDo(print())
            .andExpect(status().is4xxClientError()); 
            // the ControllerAdvice catches the Exception and returns 400 BAD REQUEST instead of a 5xx server error
    }

    @Test
    void testRegisterUser_notAlreadyRegisteredUserShouldBeCreated() throws Exception {

        doNothing()
            .when(userService).registerUser(
                argThat((user) -> user.getEmail().equals("clark_k@dailyplanet.net"))
            );

        mockMvc.perform(
                post("/api/user/new")
                    .accept(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"clark_k@dailyplanet.net\",\"password\":\"qweR42!\",\"role\":\"USER\",\"accountEnabled\":\"false\",\"firstName\":\"Clark\",\"lastName\":\"Kent\",\"about\":\"Sometimes I feel like an alien here.\"}")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isCreated());

        verify(userService, times(1)).registerUser(any(User.class));
    }

    @Test
    void testRegisterUser_existingUserShouldReturnBadRequestError() throws Exception {

        doThrow(new IllegalArgumentException())
            .when(userService).registerUser(
                argThat((user) -> user.getEmail().equals("batman@waynecorp.com"))
            );

        mockMvc.perform(
                post("/api/user/new")
                    .accept(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"batman@waynecorp.com\",\"password\":\"qweR42!\",\"role\":\"ADMIN\",\"accountEnabled\":true,\"firstName\":\"Bruce\",\"lastName\":\"Wayne\",\"about\":\"When I am not driving around in Gotham, I spend most of my time in my own cave.\"}")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().is4xxClientError());

        verify(userService, times(1)).registerUser(any(User.class));
    }

    @Test
    void testRegisterUser_nullIdUserShouldReturnBadRequestError() throws Exception {

        doThrow(new IllegalArgumentException())
            .when(userService).registerUser(
                argThat((user) -> (user.getEmail() == null))
            );

        mockMvc.perform(
                post("/api/user/new")
                    .accept(MediaType.APPLICATION_JSON)
                    .content("{}")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().is4xxClientError());

        verify(userService, times(1)).registerUser(any(User.class));
    }

    @Test
    void testUpdateUserData_existingUserShouldBeProcessed() throws Exception {

        doNothing()
            .when(userService).updateUserData(
                argThat((user) -> user.getEmail().equals("batman@waynecorp.com"))
            );

        mockMvc.perform(
                put("/api/user/update")
                    .accept(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"batman@waynecorp.com\",\"password\":\"nEwSecrEtPW700&#\",\"role\":\"ADMIN\",\"accountEnabled\":true,\"firstName\":\"Bruce\",\"lastName\":\"Wayne\",\"about\":\"When I am not driving around in Gotham, I spend most of my time in my own cave.\"}")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isAccepted());
            
        verify(userService, times(1)).updateUserData(any(User.class));
    }

    @Test
    void testUpdateUserData_notRegisteredUserShouldReturnBadRequestError() throws Exception {

        doThrow(new NoSuchElementException())
            .when(userService).updateUserData(
                argThat((user) -> (user.getEmail().equals("clark_k@dailyplanet.net")))
            );
        
        mockMvc.perform(
                put("/api/user/update")
                    .accept(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"clark_k@dailyplanet.net\",\"password\":\"99MyNewSuperHeroPassword!99\",\"role\":\"ADMIN\",\"accountEnabled\":\"false\",\"firstName\":\"Clark\",\"lastName\":\"Kent\",\"about\":\"Sometimes I feel like an alien here.\"}")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().is4xxClientError());
            
        verify(userService, times(1)).updateUserData(any(User.class));
    }

    @Test
    void testUpdateUserData_nullIdUserShouldReturnBadRequestError() throws Exception {
        
        doThrow(new IllegalArgumentException())
            .when(userService).updateUserData(
                argThat((user) -> (user.getEmail() == null))
            );

        mockMvc.perform(
                put("/api/user/update")
                    .accept(MediaType.APPLICATION_JSON)
                    .content("{}")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().is4xxClientError());

        verify(userService, times(1)).updateUserData(any(User.class));
    }
}
