package rd.useridentity.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserSecurityDTO {
    
    private String email;
    private String password;
    private String role;
    private Boolean enabled;

}
