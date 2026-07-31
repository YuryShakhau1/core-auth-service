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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private static final long ACCESS_EXPIRATION = 50;
    private static final long REFRESH_EXPIRATION = 100;
    private static final String SECRET = UUID.randomUUID().toString();

    @Mock
    private UserRoleService userRoleService;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        var securityProps = new SecurityProps();
        securityProps.setSecret(SECRET);
        securityProps.setAccessExpiration(ACCESS_EXPIRATION);
        securityProps.setRefreshExpiration(REFRESH_EXPIRATION);

        jwtService = new JwtService(securityProps, userRoleService);
    }

    @Test
    void shouldGenerateAccessTokenWhenUserExists() {
        UUID userId = UUID.randomUUID();

        when(userRoleService.findUserRoles(userId)).thenReturn(List.of("ROLE_USER", "ROLE_ADMIN"));

        String token = jwtService.generateAccessToken(userId);

        assertNotNull(token);

        Claims claims = jwtService.getClaims(token);

        assertNotNull(claims);
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals(
                List.of("ROLE_USER", "ROLE_ADMIN"),
                claims.get("roles"));

        Duration lifetime = Duration.between(
                claims.getIssuedAt().toInstant(),
                claims.getExpiration().toInstant());

        assertEquals(Duration.ofMinutes(ACCESS_EXPIRATION), lifetime);

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

        assertEquals(Duration.ofMinutes(REFRESH_EXPIRATION), lifetime);
    }

    @Test
    void shouldGenerateRefreshTokenWithRandomSessionIdWhenSessionIdIsNull() {
        UUID userId = UUID.randomUUID();

        String token = jwtService.generateRefreshToken(userId, null);

        Claims claims = jwtService.getClaims(token);
        String sessionId = claims.get("session_id", String.class);

        assertNotNull(claims);
        assertDoesNotThrow(() -> UUID.fromString(sessionId));
    }

    @Test
    void shouldReturnTrueWhenTokenIsValid() {
        UUID userId = UUID.randomUUID();

        when(userRoleService.findUserRoles(userId)).thenReturn(List.of("ROLE_USER"));

        String token = jwtService.generateAccessToken(userId);

        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void shouldReturnFalseWhenTokenIsInvalid() {
        assertFalse(jwtService.isTokenValid("invalid-token"));
    }

    @Test
    void shouldReturnClaimsWhenTokenIsValid() {
        UUID userId = UUID.randomUUID();

        when(userRoleService.findUserRoles(userId)).thenReturn(List.of("ROLE_USER"));

        String token = jwtService.generateAccessToken(userId);

        Claims claims = jwtService.getClaims(token);

        assertNotNull(claims);
        assertEquals(userId.toString(), claims.getSubject());
    }

    @Test
    void shouldReturnNullWhenTokenCannotBeParsed() {
        Claims claims = jwtService.getClaims("invalid-token");

        assertNull(claims);
    }

    @Test
    void shouldReturnFalseWhenTokenIsExpired() {
        SecurityProps props = new SecurityProps();
        props.setSecret(SECRET);
        props.setAccessExpiration(0L);
        props.setRefreshExpiration(REFRESH_EXPIRATION);

        JwtService expiredJwtService = new JwtService(props, userRoleService);

        UUID userId = UUID.randomUUID();

        when(userRoleService.findUserRoles(userId)).thenReturn(List.of("ROLE_USER"));

        String token = expiredJwtService.generateAccessToken(userId);

        assertFalse(expiredJwtService.isTokenValid(token));
    }
}
