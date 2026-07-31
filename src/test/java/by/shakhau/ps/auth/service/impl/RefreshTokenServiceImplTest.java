package by.shakhau.ps.auth.service.impl;

import by.shakhau.ps.auth.model.RefreshToken;
import by.shakhau.ps.auth.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    private static final String TOKEN_HASH = UUID.randomUUID().toString();
    private static final String REFRESH_TOKEN = UUID.randomUUID().toString();
    private static final String NEW_REFRESH_TOKEN = UUID.randomUUID().toString();

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenRepository repository;

    @Mock
    private Claims claims;

    @InjectMocks
    private RefreshTokenServiceImpl service;

    @Test
    void shouldReturnTokenHashWhenTokenExists() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        when(repository.findTokenHashByUserIdAndSessionId(userId, sessionId)).thenReturn(Optional.of(TOKEN_HASH));

        String result = service.findTokenHashByUserIdAndSessionId(userId, sessionId);

        assertEquals(TOKEN_HASH, result);
        verify(repository).findTokenHashByUserIdAndSessionId(userId, sessionId);
    }

    @Test
    void shouldReturnNullWhenTokenDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        when(repository.findTokenHashByUserIdAndSessionId(userId, sessionId)).thenReturn(Optional.empty());

        String result = service.findTokenHashByUserIdAndSessionId(userId, sessionId);

        assertNull(result);
        verify(repository).findTokenHashByUserIdAndSessionId(userId, sessionId);
    }

    @Test
    void shouldReturnRefreshTokensWhenFindByUserId() {
        UUID userId = UUID.randomUUID();
        List<RefreshToken> tokens = List.of(
                refreshToken(userId, UUID.randomUUID()),
                refreshToken(userId, UUID.randomUUID()));

        when(repository.findByUserId(userId)).thenReturn(tokens);

        List<RefreshToken> result = service.findByUserId(userId);

        assertEquals(tokens, result);
        verify(repository).findByUserId(userId);
    }

    @Test
    void shouldReturnTokenCountWhenCountByUserId() {
        UUID userId = UUID.randomUUID();

        when(repository.countByUserId(userId)).thenReturn(2L);

        long result = service.countByUserId(userId);

        assertEquals(2L, result);
        verify(repository).countByUserId(userId);
    }

    @Test
    void shouldSaveRefreshTokenWhenSaveIsCalled() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        when(jwtService.getClaims(REFRESH_TOKEN)).thenReturn(claims);
        when(claims.get("session_id")).thenReturn(sessionId.toString());

        ArgumentCaptor<RefreshToken> tokenCaptor =
                ArgumentCaptor.forClass(RefreshToken.class);

        service.save(userId, REFRESH_TOKEN);

        verify(repository).save(tokenCaptor.capture());

        RefreshToken saved = tokenCaptor.getValue();

        assertEquals(userId, saved.getUserId());
        assertEquals(sessionId, saved.getSessionId());
        assertEquals(DigestUtils.sha256Hex(REFRESH_TOKEN), saved.getTokenHash());

        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getExpiryDate());

        Instant created = saved.getCreatedAt().toInstant();
        Instant expiry = saved.getExpiryDate().toInstant();

        assertEquals(Duration.ofMinutes(15), Duration.between(created, expiry));
    }

    @Test
    void shouldUpdateTokenHashWhenUpdateTokenIsCalled() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        service.updateToken(userId, sessionId, NEW_REFRESH_TOKEN);

        verify(repository).updateTokenHash(
                userId, sessionId, DigestUtils.sha256Hex(NEW_REFRESH_TOKEN));
    }

    @Test
    void shouldDeleteTokenWhenDeleteByUserIdAndSessionIdIsCalled() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        service.deleteByUserIdAndSessionId(userId, sessionId);

        verify(repository).deleteByUserIdAndSessionId(userId, sessionId);
    }

    @Test
    void shouldDeleteAllUserTokensWhenDeleteByUserIdIsCalled() {
        UUID userId = UUID.randomUUID();

        service.deleteByUserId(userId);

        verify(repository).deleteByUserId(userId);
    }

    @Test
    void shouldDeleteAllTokensBySessionIdsWhenDeleteAllIsCalled() {
        UUID userId = UUID.randomUUID();

        UUID sessionId1 = UUID.randomUUID();
        UUID sessionId2 = UUID.randomUUID();

        RefreshToken token1 = refreshToken(userId, sessionId1);
        RefreshToken token2 = refreshToken(userId, sessionId2);

        service.deleteAll(List.of(token1, token2));

        verify(repository).deleteAllById(List.of(sessionId1, sessionId2));
    }

    private RefreshToken refreshToken(UUID userId, UUID sessionId) {
        RefreshToken token = new RefreshToken();
        token.setUserId(userId);
        token.setSessionId(sessionId);
        return token;
    }
}
