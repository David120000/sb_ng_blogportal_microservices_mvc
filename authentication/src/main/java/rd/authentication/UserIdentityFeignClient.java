package rd.authentication;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("users")
public interface UserIdentityFeignClient {
    
    @GetMapping("/api/user/security/{user-email}")
    ResponseEntity<User> getUser(@PathVariable(name="user-email") String email);
    
}
