package rd.authentication;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtility {
    
    @Value("${application.jwt.secretkey:demokey}")
    private String SECRETKEY; 

    @Value("${application.jwt.expiration:850000}")
    private Integer EXPIRATION;


    public String generateToken(String userEmail) { 

        Map<String, Object> claims = new HashMap<>();
        claims.put("messagefromauthservice", "Come to the dark side, we have cookies.");
        
        return Jwts
            .builder() 
            .setClaims(claims) 
            .setSubject(userEmail) 
            .setIssuedAt(new Date(System.currentTimeMillis())) 
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION)) 
            .signWith(this.getSignKey(), SignatureAlgorithm.HS256)
            .compact(); 
    } 
  
    private Key getSignKey() { 

        byte[] keyBytes = Decoders.BASE64.decode(SECRETKEY); 
        return Keys.hmacShaKeyFor(keyBytes); 
    } 

    public boolean validateToken(String token) { 

        boolean validToken = false;

        JwtParser jwtParser = Jwts.parserBuilder()
                                    .setSigningKey(this.getSignKey())                    
                                    .build();
        
        try {
            jwtParser.parseClaimsJws(token);
            validToken = true;
        }
        catch(Exception e) {
            throw new IllegalArgumentException("Could not verify JWT token.", e);
        }
        
        return validToken;
    } 
  
    public String extractUserEmail(String token) { 

        return this.extractClaim(token, Claims::getSubject); 
    } 
  
    public Date extractExpiration(String token) { 

        return this.extractClaim(token, Claims::getExpiration); 
    } 

    public boolean isTokenExpired(String token) { 

        return extractExpiration(token).before(new Date()); 
    } 
  
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) { 

        final Claims claims = extractAllClaims(token); 
        return claimsResolver.apply(claims); 
    } 
  
    public Claims extractAllClaims(String token) { 

        return Jwts 
                .parserBuilder() 
                .setSigningKey(this.getSignKey()) 
                .build() 
                .parseClaimsJws(token) 
                .getBody(); 
    } 
    
}
