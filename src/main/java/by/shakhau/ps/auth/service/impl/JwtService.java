package by.shakhau.ps.auth.service.impl;

import by.shakhau.ps.auth.config.SecurityProps;
import by.shakhau.ps.auth.service.UserRoleService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long accessExpiration;
    private final long refreshExpiration;
    private final UserRoleService userRoleService;

    public JwtService(SecurityProps securityProps, UserRoleService userRoleService) {
        this.secretKey = Keys.hmacShaKeyFor(securityProps.getSecret().getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = securityProps.getAccessExpiration();
        this.refreshExpiration = securityProps.getRefreshExpiration();
        this.userRoleService = userRoleService;
    }

    public String generateAccessToken(UUID userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("roles", userRoleService.findUserRoles(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessExpiration, ChronoUnit.MINUTES)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(UUID userId, String sessionId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("session_id", Optional.ofNullable(sessionId).orElseGet(() -> UUID.randomUUID().toString()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(refreshExpiration, ChronoUnit.MINUTES)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }
}
