package com.ticketflow.auth.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Issues signed HS256 JWTs. The secret and lifetime come from shared config
 * ({@code ticketflow.jwt.*}); the gateway verifies tokens with the same secret.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${ticketflow.jwt.secret}") String secret,
                      @Value("${ticketflow.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String email, String displayName) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claim("name", displayName)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(key)
                .compact();
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
