package com.spliteasy.expense_sharing.config;

import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // Initialize HMAC-SHA key from secret
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public SecretKey getKey() {
        return key;
    }

    /**
     * Extract the email/username from the JWT token
     */
    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    /**
     * Extract the user's name from the JWT token
     */
    public String extractName(String token) {
        return extractClaim(token, claims -> claims.get("name", String.class));
    }

    /**
     * Extract the user's role from the JWT token
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Generic method to extract any claim from JWT
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }
}
