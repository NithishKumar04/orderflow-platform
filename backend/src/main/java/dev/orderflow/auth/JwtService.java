package dev.orderflow.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final Duration tokenTtl;

    public JwtService(
            @Value("${orderflow.auth.jwt-secret}") String jwtSecret,
            @Value("${orderflow.auth.token-ttl}") Duration tokenTtl
    ) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.tokenTtl = tokenTtl;
    }

    public String issue(String email, String displayName) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claims(Map.of("name", displayName, "role", "CUSTOMER"))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(tokenTtl)))
                .signWith(secretKey)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
