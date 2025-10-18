package com.spliteasy.expense_sharing.util;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.spliteasy.expense_sharing.config.JwtConfig;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtConfig jwtConfig;  

   
    public SecretKey getKey() {
        return jwtConfig.getKey();
    }

    public String generateToken(String email, String role) {
        long expirationMillis = 1000 * 60 * 60 * 5; // 5 hours
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .claim("email", email) 
                .claim("name", email)  
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Methods to extract claims from token
    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(getKey())
                   .build()
                   .parseClaimsJws(token)
                   .getBody()
                   .get("email", String.class);
    }

    public String extractRole(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(getKey())
                   .build()
                   .parseClaimsJws(token)
                   .getBody()
                   .get("role", String.class);
    }

    public String extractName(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(getKey())
                   .build()
                   .parseClaimsJws(token)
                   .getBody()
                   .get("name", String.class);
    }
}
