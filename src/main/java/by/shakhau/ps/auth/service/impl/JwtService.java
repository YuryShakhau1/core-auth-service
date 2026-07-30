package by.shakhau.ps.auth.service.impl;

import by.shakhau.ps.auth.config.SecurityProps;
import by.shakhau.ps.auth.controller.exception.UnauthorizedException;
import by.shakhau.ps.auth.service.UserRoleService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final long accessExpiration;
    private final long refreshExpiration;
    private final UserRoleService userRoleService;

    @Getter
    private final String publicKeyAsString;

    @RequiredArgsConstructor
    @Getter
    public static class TokenInfo {

        private final String token;
        private final String sessionId;
    }

    public JwtService(SecurityProps securityProps, UserRoleService userRoleService) {
        this.accessExpiration = securityProps.getAccessExpiration();
        this.refreshExpiration = securityProps.getRefreshExpiration();
        this.userRoleService = userRoleService;

        KeyPair keyPair = generateKeyPairFromPassword(securityProps.getSecret());
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();

        byte[] keyBytes = this.publicKey.getEncoded();
        String base64Key = Base64.getEncoder().encodeToString(keyBytes);
        String formattedKey = base64Key.replaceAll("(.{64})", "$1\n");
        this.publicKeyAsString = "-----BEGIN PUBLIC KEY-----\n" + formattedKey + "\n-----END PUBLIC KEY-----";
    }

    public TokenInfo generateAccessToken(UUID userId, String sessionId) {
        Instant now = Instant.now();
        String currentSessionId = (sessionId == null ? UUID.randomUUID().toString() : sessionId);
        String accessToken = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("session_id", currentSessionId)
                .claim("roles", userRoleService.findUserRoles(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessExpiration, ChronoUnit.SECONDS)))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
        return new TokenInfo(accessToken, currentSessionId);
    }

    public TokenInfo generateAccessToken(UUID userId) {
        return generateAccessToken(userId, null);
    }

    public String generateRefreshToken(UUID userId, String sessionId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("session_id", sessionId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(refreshExpiration, ChronoUnit.SECONDS)))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = getClaims(token);
            return claims != null && claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    private KeyPair generateKeyPairFromPassword(String password) {
        try {
            SecureRandom deterministicRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");
            deterministicRandom.setSeed(password.getBytes(StandardCharsets.UTF_8));

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048, deterministicRandom);

            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate RSA keys from password", e);
        }
    }
}
