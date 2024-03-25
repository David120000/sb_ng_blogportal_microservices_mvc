package rd.authentication.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class User {
    
    private String email;
    private String password;
    private String role;
    private boolean enabled;
    
}
