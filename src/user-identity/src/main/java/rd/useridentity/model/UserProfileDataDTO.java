package rd.useridentity.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserProfileDataDTO {
    
    private String email;
    private String name;
    private String about;

}
