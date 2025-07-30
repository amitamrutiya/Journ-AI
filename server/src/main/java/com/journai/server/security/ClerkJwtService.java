package com.journai.server.security;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.journai.server.config.AppProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class ClerkJwtService {

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private WebClient webClient;

    public String validateTokenAndGetUserId(String token) {
        try {
            // For simplicity, we'll extract the user ID from the token without full
            // validation
            // In a production environment, you should validate the token signature with
            // Clerk's public keys

            String secretKey = appProperties.getClerk().getSecretKey();
            if (secretKey == null || secretKey.isEmpty()) {
                throw new RuntimeException("Clerk secret key not configured");
            }

            // Create a secret key for validation (this is a simplified approach)
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();

        } catch (Exception e) {
            // For development, try to extract user ID from token payload without validation
            try {
                String[] parts = token.split("\\.");
                if (parts.length >= 2) {
                    String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                    // Simple JSON parsing to extract sub claim
                    if (payload.contains("\"sub\":")) {
                        int start = payload.indexOf("\"sub\":\"") + 7;
                        int end = payload.indexOf("\"", start);
                        return payload.substring(start, end);
                    }
                }
            } catch (Exception ex) {
                // Ignore and return null
            }
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getClerkUserInfo(String userId) {
        try {
            String secretKey = appProperties.getClerk().getSecretKey();
            return webClient.get()
                    .uri("https://api.clerk.com/v1/users/" + userId)
                    .header("Authorization", "Bearer " + secretKey)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch user info from Clerk", e);
        }
    }
}
