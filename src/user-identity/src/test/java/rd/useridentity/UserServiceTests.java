package rd.useridentity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import rd.useridentity.model.User;
import rd.useridentity.model.UserProfileDataDTO;
import rd.useridentity.model.UserSecurityDTO;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTests {

    @InjectMocks
    private UserService userService;

    private User user1;
    private User user2;
    private User user3;

    @Mock
    private InputValidator validator;

    @Mock
    private UserToDTOMapper mapper;

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;


    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.openMocks(this);

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
        
        user3 = User.builder().build();
    }


    @Test
    void testCheckIfUserExistsInDb() {

        doReturn(true).when(repository).existsById("batman@waynecorp.com");
        doReturn(false).when(repository).existsById("clark_k@dailyplanet.net");
        doThrow(new IllegalArgumentException()).when(repository).existsById(null);


        assertTrue(
            userService.checkIfUserExistsInDb(user1.getEmail())
        );
        assertFalse(
            userService.checkIfUserExistsInDb(user2.getEmail())
        );
        assertThrows(
            IllegalArgumentException.class, 
            () -> userService.checkIfUserExistsInDb(user3.getEmail())
        );
    }

    @Test
    void testDeleteUserById() {

        doNothing().when(repository).deleteById(anyString());
        doThrow(new IllegalArgumentException()).when(repository).deleteById(null);


        assertDoesNotThrow(
            () -> userService.deleteUserById(user1.getEmail())
        );
        assertThrows(
            IllegalArgumentException.class, 
            () -> userService.deleteUserById(user3.getEmail())
        );
    }

    @Test
    void testGetUserProfileData_registeredUserShouldSucceed() {

        when(repository.findById("batman@waynecorp.com"))
            .thenReturn(Optional.of(user1));

        when(mapper.buildProfileDataDTO(user1))
            .thenCallRealMethod();

        UserProfileDataDTO dtoUser1 = userService.getUserProfileData(user1.getEmail());
        
        assertEquals(user1.getEmail(), dtoUser1.getEmail());
        assertEquals(user1.getFirstName() + " " + user1.getLastName(), dtoUser1.getName());
        assertEquals(user1.getAbout(), dtoUser1.getAbout());
    }

    @Test
    void testGetUserProfileData_notRegisteredUserShouldThrow() {

        doThrow(new NoSuchElementException())
            .when(repository).findById("clark_k@dailyplanet.net");
        
        assertThrows(
            NoSuchElementException.class, 
            () -> userService.getUserProfileData(user2.getEmail())
        );
    }

    @Test
    void testGetUserProfileData_nullIdUserShouldThrow() {

        doThrow(new IllegalArgumentException())
            .when(repository).findById(null);
        
        assertThrows(
            IllegalArgumentException.class, 
            () -> userService.getUserProfileData(user3.getEmail())
        );
    }

    @Test
    void testGetUserSecurityProfile_registeredUserShouldSucceed() {

        when(repository.findById("batman@waynecorp.com"))
            .thenReturn(Optional.of(user1));

        when(mapper.buildSecurityDTO(user1))
            .thenCallRealMethod();
        
        UserSecurityDTO dtoUser1 = userService.getUserSecurityProfile(user1.getEmail());

        assertEquals(user1.getEmail(), dtoUser1.getEmail());
        assertEquals(user1.getPassword(), dtoUser1.getPassword());
        assertEquals(user1.getRole(), dtoUser1.getRole());
        assertEquals(user1.getAccountEnabled(), dtoUser1.getEnabled());

    }

    @Test
    void testGetUserSecurityProfile_notRegisteredUserShouldThrow() {

        doThrow(new NoSuchElementException())
            .when(repository).findById("clark_k@dailyplanet.net");
        

        assertThrows(
            NoSuchElementException.class, 
            () -> userService.getUserSecurityProfile(user2.getEmail())
        );
    }

    @Test
    void testGetUserSecurityProfile_nullIdUserShouldThrow() {

        doThrow(new IllegalArgumentException())
            .when(repository).findById(null);
        
        assertThrows(
            IllegalArgumentException.class, 
            () -> userService.getUserSecurityProfile(user3.getEmail())
        );
    }

    @Test
    void testRegisterUser_notRegisteredUserShouldSucceed() {

        doReturn(false).when(repository).existsById("clark_k@dailyplanet.net");
        doReturn(true).when(validator).validateUser(user2);
        doReturn(user2.getPassword()).when(passwordEncoder).encode(anyString());
        doReturn(user2).when(repository).save(user2);

        assertDoesNotThrow(() -> userService.registerUser(user2));
        verify(repository, times(1)).existsById(anyString());
        verify(repository, times(1)).save(any(User.class));

        InOrder inOrder = inOrder(repository);
        inOrder.verify(repository).existsById(anyString());
        inOrder.verify(repository).save(any(User.class));
    }

    @Test
    void testRegisterUser_registeredUserShouldThrow() {
  
        doReturn(true).when(repository).existsById("batman@waynecorp.com");
        
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(user1));
    }

    @Test
    void testRegisterUser_nullIdUserShouldThrow() {

        Throwable throwable = 
            assertThrows(IllegalArgumentException.class, () -> userService.registerUser(user3));

        assertEquals("Email field is required. It cannot be empty (null).", throwable.getMessage());
    }

    @Test
    void testUpdateUserData_registeredUserShouldSucceed() {

        doReturn(true).when(repository).existsById("batman@waynecorp.com");
        doReturn(true).when(validator).validateUser(user1);
        doReturn(user1.getPassword()).when(passwordEncoder).encode(anyString());
        doReturn(user1).when(repository).save(user1);

        assertDoesNotThrow(() -> userService.updateUserData(user1));
        verify(repository, times(1)).existsById(anyString());
        verify(repository, times(1)).save(any(User.class));

        InOrder inOrder = inOrder(repository);
        inOrder.verify(repository).existsById(anyString());
        inOrder.verify(repository).save(any(User.class));
    }

    @Test
    void testUpdateUserData_notRegisteredUserShouldThrow() {

        doReturn(false).when(repository).existsById("clark_k@dailyplanet.net");

        assertThrows(NoSuchElementException.class, () -> userService.updateUserData(user2));
    }

    @Test
    void testUpdateUserData_nullIdUserShouldThrow() {

        Throwable throwable = 
            assertThrows(IllegalArgumentException.class, () -> userService.updateUserData(user3));

        assertEquals("Email field is required. It cannot be empty (null).", throwable.getMessage());
    }
}
