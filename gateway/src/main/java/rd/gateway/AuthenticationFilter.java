package rd.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.RequiredArgsConstructor;
import rd.gateway.model.AuthToken;
import rd.gateway.model.Authorization;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GatewayFilterFactory<AuthenticationFilter.Config>  {

    private final RouterValidator validator;
    private final AuthenticationClient authClient;
    
    @Override
    public GatewayFilter apply(Config config) {
        
        return new GatewayFilter() {

            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
       
                ServerHttpRequest request = exchange.getRequest();

                if(validator.isSecured.test(request)) {
                    
                    if(this.authMissing(request)) {
                        return this.onError(exchange, HttpStatus.UNAUTHORIZED);
                    }

                    final String jwt = request.getHeaders().getOrEmpty("Authorization").get(0);

                    try {
                        final Authorization userAuthorization = authClient.authorize( AuthToken.builder().jwt(jwt).build() );
                        
                        if(!userAuthorization.getAuthenticated()) {  
                            return this.onError(exchange, HttpStatus.UNAUTHORIZED);
                        }
                    }
                    catch(RuntimeException ex) {
                        return this.onError(exchange, HttpStatus.FORBIDDEN);
                    }  

                }

                return chain.filter(exchange);
            }

            private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {

                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(httpStatus);

                return response.setComplete();
            }       

            private boolean authMissing(ServerHttpRequest request) {
                return !request.getHeaders().containsKey("Authorization");
            }
        };
    }

    @Override
    public Class<AuthenticationFilter.Config> getConfigClass() {
        return AuthenticationFilter.Config.class;
    }

    public static class Config {
        
    }

}
