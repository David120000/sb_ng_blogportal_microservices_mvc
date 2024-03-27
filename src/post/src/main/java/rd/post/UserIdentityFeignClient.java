package rd.post;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("users")
public interface UserIdentityFeignClient {
    
    @GetMapping("/api/user/exists/{user-email}")
    ResponseEntity<Boolean> checkIfUserExists(@PathVariable(name="user-email") String email);

}
