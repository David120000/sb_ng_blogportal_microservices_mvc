package rd.authentication.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserAuthorization {
    
    private String subjectId;
    private Boolean authenticated;
    private String role;

}
