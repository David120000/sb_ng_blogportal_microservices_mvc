package rd.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import rd.gateway.model.AuthToken;
import rd.gateway.model.Authorization;

@Component
public class AuthenticationClient {
    
    @Value("${application.config.authenticationservice-url}")
    private String authenticationServiceURL;
    private final RestClient restClient = RestClient.create();


    public Authorization authorize(AuthToken token) throws RuntimeException {

        var authorization = restClient.post()
            .uri(authenticationServiceURL)
            .contentType(MediaType.APPLICATION_JSON)
            .body(token)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                throw new RuntimeException("Error: " + response.getStatusCode() + " :: " + response.getStatusText());
            })
            .body(Authorization.class);

        return authorization;
    }
}
