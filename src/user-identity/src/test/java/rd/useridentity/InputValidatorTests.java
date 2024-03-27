package rd.useridentity;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import rd.useridentity.model.User;

import static org.junit.jupiter.api.Assertions.*;

public class InputValidatorTests {

    private static InputValidator inputValidator;


    @BeforeAll
    static void beforeAll() {
        inputValidator = new InputValidator();
    }


    @Test
    void testValidateUser_shouldValidate() {

        User user = User.builder()
            .email("batman@waynecorp.com")
            .password("qweR42!")
            .role("ADMIN")
            .accountEnabled(true)
            .firstName("Bruce")
            .lastName("Wayne")
            .about("When I am not driving around in Gotham, I spend most of my time in my own cave.")
            .build();

        assertTrue(inputValidator.validateUser(user));
        assertNotNull(inputValidator.validateUser(user));
        assertDoesNotThrow(() -> inputValidator.validateUser(user));
    }

    @Test
    void testValidateUser_badPasswordShouldTrowException1() {

        User user1 = User.builder()
            .email("clark_k@dailyplanet.net")
            .password("justletmein")
            .role("USER")
            .accountEnabled(true)
            .firstName("Clark")
            .lastName("Kent")
            .about("Sometimes I feel like an alien here.")
            .build();
        
        Throwable throwable = 
            assertThrows(IllegalArgumentException.class, () -> inputValidator.validateUser(user1));
        
        assertEquals(IllegalArgumentException.class, throwable.getClass());
        assertTrue(throwable.getMessage().startsWith("The given password is invalid."));
        assertEquals(
            "The given password is invalid. " +
            "The password should be at least 6 characters long and contain all of the following: " +
            "lower case letter, upper case letter, number, any special character: _-!@#$&*", 
            throwable.getMessage()
        ); 
    }

    @Test
    void testValidateUser_badPasswordShouldTrowException2() {

        User user2 = User.builder()
            .email("clark_k@dailyplanet.net")
            .password("JustLetMeIn!")
            .role("USER")
            .accountEnabled(true)
            .firstName("Clark")
            .lastName("Kent")
            .about("Sometimes I feel like an alien here.")
            .build();
        
        Throwable throwable = 
            assertThrows(IllegalArgumentException.class, () -> inputValidator.validateUser(user2));
        
        assertEquals(IllegalArgumentException.class, throwable.getClass());
        assertTrue(throwable.getMessage().startsWith("The given password is invalid."));
        assertEquals(
            "The given password is invalid. " +
            "The password should be at least 6 characters long and contain all of the following: " +
            "lower case letter, upper case letter, number, any special character: _-!@#$&*", 
            throwable.getMessage()
        ); 
    }

    @Test
    void testValidateUser_badPasswordShouldTrowException3() {

        User user3 = User.builder()
            .email("clark_k@dailyplanet.net")
            .password("JustLetMeIn999")
            .role("USER")
            .accountEnabled(true)
            .firstName("Clark")
            .lastName("Kent")
            .about("Sometimes I feel like an alien here.")
            .build();
        
        Throwable throwable = 
            assertThrows(IllegalArgumentException.class, () -> inputValidator.validateUser(user3));
        
        assertEquals(IllegalArgumentException.class, throwable.getClass());
        assertTrue(throwable.getMessage().startsWith("The given password is invalid."));
        assertEquals(
            "The given password is invalid. " +
            "The password should be at least 6 characters long and contain all of the following: " +
            "lower case letter, upper case letter, number, any special character: _-!@#$&*", 
            throwable.getMessage()
        ); 
    }

    @Test
    void testValidateUser_shortPasswordShouldTrowException() {

        User user4 = User.builder()
            .email("clark_k@dailyplanet.net")
            .password("!S!1")
            .role("USER")
            .accountEnabled(true)
            .firstName("Clark")
            .lastName("Kent")
            .about("Sometimes I feel like an alien here.")
            .build();
        
        Throwable throwable = 
            assertThrows(IllegalArgumentException.class, () -> inputValidator.validateUser(user4));
        
        assertEquals(IllegalArgumentException.class, throwable.getClass());
        assertTrue(throwable.getMessage().startsWith("The given password is invalid."));
        assertEquals(
            "The given password is invalid. " +
            "The password should be at least 6 characters long and contain all of the following: " +
            "lower case letter, upper case letter, number, any special character: _-!@#$&*", 
            throwable.getMessage()
        ); 
    }

    @Test
    void testValidateUser_badEmailAddressShouldTrowException() {

        User user = User.builder()
            .email("whatsanemail.com")
            .password("JustLetMeIn999")
            .role("USER")
            .accountEnabled(true)
            .firstName("Bob")
            .lastName("Smith")
            .about(null)
            .build();
        
        Throwable throwable = 
            assertThrows(IllegalArgumentException.class, () -> inputValidator.validateUser(user));
        
        assertEquals(IllegalArgumentException.class, throwable.getClass());
        assertTrue(throwable.getMessage().startsWith("The given email address"));
        assertEquals(
            "The given email address (" + user.getEmail() + ") is invalid.", 
            throwable.getMessage()
        ); 
    }

    @Test
    void testValidateUser_emailAddressWithNumbersAndSpecialCharactersShouldValidate() {

        User user = User.builder()
            .email("bob's-po$tb0x10@mail-service-provider.co.au")
            .password("_-!MySecret23@#&")
            .role("USER")
            .accountEnabled(false)
            .firstName("Bob")
            .lastName("Smith")
            .about(null)
            .build();
        
        assertTrue(inputValidator.validateUser(user));
        assertNotNull(inputValidator.validateUser(user));
        assertDoesNotThrow(() -> inputValidator.validateUser(user));
    }

    @Test
    void testValidateUser_noAllowedRoleShouldThrowException() {

        User user = User.builder()
            .email("batman@waynecorp.com")
            .password("qweR42!")
            .role("SUPERHERO")
            .accountEnabled(true)
            .firstName("Bruce")
            .lastName("Wayne")
            .about("When I am not driving around in Gotham, I spend most of my time in my own cave.")
            .build();
        
        Throwable throwable = 
            assertThrows(IllegalArgumentException.class, () -> inputValidator.validateUser(user));
        
        assertEquals(IllegalArgumentException.class, throwable.getClass());
        assertTrue(throwable.getMessage().startsWith("The given user role"));
        assertEquals(
            "The given user role (" + user.getRole() + ") is invalid.", 
            throwable.getMessage()
        ); 
    }

    @Test
    void testValidateUser_nullEmailShouldThrowException() {

        User user = User.builder()
            .email(null)
            .password("_-!MySecret23@#&")
            .role("USER")
            .accountEnabled(false)
            .firstName("Bob")
            .lastName("Smith")
            .about(null)
            .build();
        
        Throwable throwable = 
            assertThrows(IllegalArgumentException.class, () -> inputValidator.validateUser(user));
        
        assertEquals(IllegalArgumentException.class, throwable.getClass());
        assertEquals("Email field is required. It cannot be empty (null).", throwable.getMessage());
    }

    @Test
    void testValidateUser_nullPasswordShouldThrowException() {

        User user = User.builder()
            .email("bob_s@mail.com")
            .password(null)
            .role("USER")
            .accountEnabled(false)
            .firstName("Bob")
            .lastName("Smith")
            .about(null)
            .build();
        
        Throwable throwable = 
            assertThrows(IllegalArgumentException.class, () -> inputValidator.validateUser(user));
        
        assertEquals(IllegalArgumentException.class, throwable.getClass());
        assertEquals("Password field is required. It cannot be empty (null).", throwable.getMessage());
    }

    @Test
    void testValidateUser_nullRoleShouldThrowException() {

        User user = User.builder()
            .email("bob_s@mail.com")
            .password("_-!MySecret23@#&")
            .role(null)
            .accountEnabled(false)
            .firstName("Bob")
            .lastName("Smith")
            .about(null)
            .build();
        
        Throwable throwable = 
            assertThrows(IllegalArgumentException.class, () -> inputValidator.validateUser(user));
        
        assertEquals(IllegalArgumentException.class, throwable.getClass());
        assertTrue(throwable.getMessage().startsWith("The given user role"));
        assertEquals(
            "The given user role (" + user.getRole() + ") is invalid.", 
            throwable.getMessage()
        ); 
    }

}
