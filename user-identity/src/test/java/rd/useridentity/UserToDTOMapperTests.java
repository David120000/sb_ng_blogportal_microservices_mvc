package rd.useridentity;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserToDTOMapperTests {

    private static UserToDTOMapper mapper;

    private User user1;
    private User user2;
    private User user3;


    @BeforeAll
    static void beforeAll() {
        mapper = new UserToDTOMapper();
    }

    @BeforeEach
    void beforeEach() {

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
            .password("justletmein")
            .role("USER")
            .accountEnabled(false)
            .firstName("Clark")
            .lastName("Kent")
            .about("Sometimes I feel like an alien here.")
            .build();
        
        user3 = User.builder().build();
    }


    @Test
    void testBuildProfileDataDTO() {

        UserProfileDataDTO dtoUser1 = mapper.buildProfileDataDTO(user1);
        UserProfileDataDTO dtoUser2 = mapper.buildProfileDataDTO(user2);
        UserProfileDataDTO dtoUser3 = mapper.buildProfileDataDTO(user3);

        assertNotNull(dtoUser1);
        assertEquals(user1.getEmail(), dtoUser1.getEmail());
        assertEquals(user1.getFirstName() + " " + user1.getLastName(), dtoUser1.getName());
        assertEquals(user1.getAbout(), dtoUser1.getAbout());
        assertDoesNotThrow(() -> mapper.buildProfileDataDTO(user1));

        assertNotNull(dtoUser2);
        assertEquals(user2.getEmail(), dtoUser2.getEmail());
        assertEquals(user2.getFirstName() + " " + user2.getLastName(), dtoUser2.getName());
        assertEquals(user2.getAbout(), dtoUser2.getAbout());
        assertDoesNotThrow(() -> mapper.buildProfileDataDTO(user2));

        assertNotNull(dtoUser3);
        assertEquals(user3.getEmail(), dtoUser3.getEmail());
        assertEquals(user3.getFirstName() + " " + user3.getLastName(), dtoUser3.getName());
        assertEquals(user3.getAbout(), dtoUser3.getAbout());
        assertDoesNotThrow(() -> mapper.buildProfileDataDTO(user3));
        assertNull(dtoUser3.getEmail());
        assertEquals("null null", dtoUser3.getName());
        assertNull(dtoUser3.getAbout());  
    }

    @Test
    void testBuildSecurityDTO() {

        UserSecurityDTO dtoUser1 = mapper.buildSecurityDTO(user1);
        UserSecurityDTO dtoUser2 = mapper.buildSecurityDTO(user2);
        UserSecurityDTO dtoUser3 = mapper.buildSecurityDTO(user3);

        assertNotNull(dtoUser1);
        assertEquals(user1.getEmail(), dtoUser1.getEmail());
        assertEquals(user1.getPassword(), dtoUser1.getPassword());
        assertEquals(user1.getRole(), dtoUser1.getRole());
        assertEquals(user1.getAccountEnabled(), dtoUser1.getEnabled());
        assertDoesNotThrow(() -> mapper.buildSecurityDTO(user1));

        assertNotNull(dtoUser2);
        assertEquals(user2.getEmail(), dtoUser2.getEmail());
        assertEquals(user2.getPassword(), dtoUser2.getPassword());
        assertEquals(user2.getRole(), dtoUser2.getRole());
        assertEquals(user2.getAccountEnabled(), dtoUser2.getEnabled());
        assertDoesNotThrow(() -> mapper.buildSecurityDTO(user2));

        assertNotNull(dtoUser3);
        assertEquals(user3.getEmail(), dtoUser3.getEmail());
        assertEquals(user3.getPassword(), dtoUser3.getPassword());
        assertEquals(user3.getRole(), dtoUser3.getRole());
        assertEquals(user3.getAccountEnabled(), dtoUser3.getEnabled());
        assertDoesNotThrow(() -> mapper.buildSecurityDTO(user3));
        assertNull(dtoUser3.getEmail());
        assertNull(dtoUser3.getPassword());
        assertNull(dtoUser3.getRole());
        assertNull(dtoUser3.getEnabled());
    }
}
