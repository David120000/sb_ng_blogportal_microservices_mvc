package rd.gateway;

import java.util.List;
import java.util.function.Predicate;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

@Service
public class RouterValidator {
    
    public static final List<String> publicEndpoints = List.of(
            "/authenticate",
            "/user/new",
            "/eureka",
            "/mongo"
    ); // for testing the /eureka and /mongo uris are public


    public Predicate<ServerHttpRequest> isSecured =
            request -> publicEndpoints.stream().noneMatch(uri -> request.getURI().getPath().contains(uri));
}
