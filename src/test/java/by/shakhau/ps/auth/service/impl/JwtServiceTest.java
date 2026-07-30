package by.shakhau.ps.auth.service.impl;

import by.shakhau.ps.auth.config.SecurityProps;
import by.shakhau.ps.auth.service.UserRoleService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private static final long ACCESS_EXPIRATION = 50;
    private static final long REFRESH_EXPIRATION = 100;
    private static final String TEST_SECRET = "my-super-stable-test-password-1234567890!";

    @Mock
    private UserRoleService userRoleService;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        SecurityProps securityProps = spy(new SecurityProps());
        securityProps.setAccessExpiration(ACCESS_EXPIRATION);
        securityProps.setRefreshExpiration(REFRESH_EXPIRATION);

        doReturn(TEST_SECRET).when(securityProps).getSecret();

        jwtService = new JwtService(securityProps, userRoleService);
    }

    @Test
    void shouldGenerateAccessTokenWhenUserExists() {
        UUID userId = UUID.randomUUID();

        when(userRoleService.findUserRoles(userId)).thenReturn(List.of("ROLE_USER", "ROLE_ADMIN"));

        JwtService.TokenInfo token = jwtService.generateAccessToken(userId);

        assertNotNull(token);

        Claims claims = jwtService.getClaims(token.getToken());

        assertNotNull(claims);
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals(
                List.of("ROLE_USER", "ROLE_ADMIN"),
                claims.get("roles"));

        Duration lifetime = Duration.between(
                claims.getIssuedAt().toInstant(),
                claims.getExpiration().toInstant());

        assertTrue(Math.abs(Duration.ofSeconds(ACCESS_EXPIRATION).toSeconds() - lifetime.toSeconds()) <= 1);

        verify(userRoleService).findUserRoles(userId);
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
        String resultSessionId = claims.get("session_id", String.class);

        assertNotNull(claims);
        assertDoesNotThrow(() -> UUID.fromString(resultSessionId));
    }

    @Test
    void shouldReturnTrueWhenTokenIsValid() {
        UUID userId = UUID.randomUUID();

        when(userRoleService.findUserRoles(userId)).thenReturn(List.of("ROLE_USER"));

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

        when(userRoleService.findUserRoles(userId)).thenReturn(List.of("ROLE_USER"));

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
        SecurityProps props = spy(new SecurityProps());
        props.setAccessExpiration(0L);
        props.setRefreshExpiration(REFRESH_EXPIRATION);
        doReturn(TEST_SECRET).when(props).getSecret();

        var expiredJwtService = new JwtService(props, userRoleService);

        UUID userId = UUID.randomUUID();

        when(userRoleService.findUserRoles(userId)).thenReturn(List.of("ROLE_USER"));

        JwtService.TokenInfo token = expiredJwtService.generateAccessToken(userId);

        assertFalse(expiredJwtService.isTokenValid(token.getToken()));
    }

    @Test
    void shouldBeDeterministicWhenGeneratedWithSamePassword() {
        UUID userId = UUID.randomUUID();
        when(userRoleService.findUserRoles(userId)).thenReturn(List.of("ROLE_USER"));

        JwtService.TokenInfo tokenFromFirstService = jwtService.generateAccessToken(userId);

        SecurityProps secondProps = spy(new SecurityProps());
        secondProps.setAccessExpiration(ACCESS_EXPIRATION);
        secondProps.setRefreshExpiration(REFRESH_EXPIRATION);
        doReturn(TEST_SECRET).when(secondProps).getSecret();

        var secondJwtService = new JwtService(secondProps, userRoleService);

        assertTrue(secondJwtService.isTokenValid(tokenFromFirstService.getToken()));

        Claims claims = secondJwtService.getClaims(tokenFromFirstService.getToken());
        assertEquals(userId.toString(), claims.getSubject());
    }
}
