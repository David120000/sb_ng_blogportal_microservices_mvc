package rd.useridentity;

import org.springframework.stereotype.Service;

@Service
public class UserToDTOMapper {
    
    public UserSecurityDTO buildSecurityDTO(User user) {

        var securityDTO = UserSecurityDTO.builder()
            .email(user.getEmail())
            .password(user.getPassword())
            .role(user.getRole())
            .enabled(user.getAccountEnabled())
            .build();
        
        return securityDTO;
    }

    public UserProfileDataDTO buildProfileDataDTO(User user) {

        var profileDataDTO = UserProfileDataDTO.builder()
            .email(user.getEmail())
            .name(user.getFirstName() + " " + user.getLastName())
            .about(user.getAbout())
            .build();

        return profileDataDTO;
    }
    
}
