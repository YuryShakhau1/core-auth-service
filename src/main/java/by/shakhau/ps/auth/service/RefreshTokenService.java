package by.shakhau.ps.auth.service;

import by.shakhau.ps.auth.model.RefreshToken;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface RefreshTokenService {

    String findTokenHashByUserIdAndSessionId(UUID userId, UUID sessionId);
    List<RefreshToken> findByUserId(UUID userId);
    long countByUserId(UUID userId);
    void save(UUID userId, String refreshToken);
    void updateToken(UUID userId, UUID sessionId, String refreshToken);
    void deleteByUserIdAndSessionId(UUID userId, UUID sessionId);
    void deleteByUserId(UUID userId);
    void deleteAll(Collection<RefreshToken> tokensToDelete);
}
