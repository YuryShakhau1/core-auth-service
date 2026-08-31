package by.shakhau.ps.auth.service.impl;

import by.shakhau.ps.auth.config.SecurityProps;
import by.shakhau.ps.auth.service.UserRoleService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private static final long ACCESS_EXPIRATION = 50;
    private static final long REFRESH_EXPIRATION = 100;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private SecurityProps securityProps;

    private JwtService jwtService;
    private String validBase64PrivateKey;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        validBase64PrivateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

        when(securityProps.getSecret()).thenReturn(validBase64PrivateKey);
        when(securityProps.getAccessExpiration()).thenReturn(ACCESS_EXPIRATION);
        when(securityProps.getRefreshExpiration()).thenReturn(REFRESH_EXPIRATION);

        jwtService = new JwtService(securityProps, userRoleService);
    }

    @Test
    void shouldGenerateAccessTokenWhenUserExists() {
        UUID userId = UUID.randomUUID();
        when(userRoleService.findUserRoleNames(userId)).thenReturn(List.of("ROLE_USER", "ROLE_ADMIN"));

        JwtService.TokenInfo token = jwtService.generateAccessToken(userId);

        assertNotNull(token);
        Claims claims = jwtService.getClaims(token.getToken());
        assertNotNull(claims);
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals(List.of("ROLE_USER", "ROLE_ADMIN"), claims.get("roles"));

        Duration lifetime = Duration.between(
                claims.getIssuedAt().toInstant(),
                claims.getExpiration().toInstant());
        assertTrue(Math.abs(Duration.ofSeconds(ACCESS_EXPIRATION).toSeconds() - lifetime.toSeconds()) <= 1);
        verify(userRoleService).findUserRoleNames(userId);
    }

    @Test
    void shouldGenerateRefreshTokenWithProvidedSessionIdWhenSessionIdIsNotNull() {
        UUID userId = UUID.randomUUID();
        String sessionId = UUID.randomUUID().toString();

        String token = jwtService.generateRefreshToken(userId, sessionId);

        Claims claims = jwtService.getClaims(token);
        assertNotNull(claims);
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals(sessionId, claims.get("session_id"));

        Duration lifetime = Duration.between(
                claims.getIssuedAt().toInstant(),
                claims.getExpiration().toInstant());
        assertTrue(Math.abs(Duration.ofSeconds(REFRESH_EXPIRATION).toSeconds() - lifetime.toSeconds()) <= 1);
    }

    @Test
    void shouldGenerateRefreshTokenWithRandomSessionIdWhenSessionIdIsNull() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        String token = jwtService.generateRefreshToken(userId, sessionId.toString());

        Claims claims = jwtService.getClaims(token);
        assertNotNull(claims);
        String resultSessionId = claims.get("session_id", String.class);
        assertDoesNotThrow(() -> UUID.fromString(resultSessionId));
    }

    @Test
    void shouldReturnTrueWhenTokenIsValid() {
        UUID userId = UUID.randomUUID();
        when(userRoleService.findUserRoleNames(userId)).thenReturn(List.of("ROLE_USER"));

        JwtService.TokenInfo token = jwtService.generateAccessToken(userId);

        assertTrue(jwtService.isTokenValid(token.getToken()));
    }

    @Test
    void shouldReturnFalseWhenTokenIsInvalid() {
        assertFalse(jwtService.isTokenValid("invalid-token"));
    }

    @Test
    void shouldReturnClaimsWhenTokenIsValid() {
        UUID userId = UUID.randomUUID();
        when(userRoleService.findUserRoleNames(userId)).thenReturn(List.of("ROLE_USER"));

        JwtService.TokenInfo token = jwtService.generateAccessToken(userId);

        Claims claims = jwtService.getClaims(token.getToken());
        assertNotNull(claims);
        assertEquals(userId.toString(), claims.getSubject());
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenTokenCannotBeParsed() {
        assertNull(jwtService.getClaims("invalid-token"));
    }

    @Test
    void shouldReturnFalseWhenTokenIsExpired() {
        SecurityProps expiredProps = mock(SecurityProps.class);
        when(expiredProps.getSecret()).thenReturn(validBase64PrivateKey);
        when(expiredProps.getAccessExpiration()).thenReturn(0L);
        when(expiredProps.getRefreshExpiration()).thenReturn(REFRESH_EXPIRATION);

        var expiredJwtService = new JwtService(expiredProps, userRoleService);
        UUID userId = UUID.randomUUID();
        when(userRoleService.findUserRoleNames(userId)).thenReturn(List.of("ROLE_USER"));

        JwtService.TokenInfo token = expiredJwtService.generateAccessToken(userId);

        assertFalse(expiredJwtService.isTokenValid(token.getToken()));
    }

    @Test
    void shouldBeDeterministicWhenGeneratedWithSamePassword() {
        UUID userId = UUID.randomUUID();
        when(userRoleService.findUserRoleNames(userId)).thenReturn(List.of("ROLE_USER"));

        JwtService.TokenInfo tokenFromFirstService = jwtService.generateAccessToken(userId);

        SecurityProps secondProps = mock(SecurityProps.class);
        when(secondProps.getSecret()).thenReturn(validBase64PrivateKey);
        when(secondProps.getAccessExpiration()).thenReturn(ACCESS_EXPIRATION);
        when(secondProps.getRefreshExpiration()).thenReturn(REFRESH_EXPIRATION);

        var secondJwtService = new JwtService(secondProps, userRoleService);

        assertTrue(secondJwtService.isTokenValid(tokenFromFirstService.getToken()));
        Claims claims = secondJwtService.getClaims(tokenFromFirstService.getToken());
        assertEquals(userId.toString(), claims.getSubject());
    }
}
