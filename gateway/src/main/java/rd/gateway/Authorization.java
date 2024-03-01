package rd.gateway;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Authorization {
    
    private String subjectId;
    private Boolean authenticated;
    private String role;
    
}
