package by.shakhau.ps.auth.service.impl;

import by.shakhau.ps.auth.model.RefreshToken;
import by.shakhau.ps.auth.repository.RefreshTokenRepository;
import by.shakhau.ps.auth.service.RefreshTokenService;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final JwtService jwtService;
    private final RefreshTokenRepository repository;

    @Override
    public String findTokenHashByUserIdAndSessionId(UUID userId, UUID sessionId) {
        return repository.findTokenHashByUserIdAndSessionId(userId, sessionId).orElse(null);
    }

    @Override
    public List<RefreshToken> findByUserId(UUID userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public long countByUserId(UUID userId) {
        return repository.countByUserId(userId);
    }

    @Override
    public void save(UUID userId, String refreshToken) {
        Claims refreshTokenClaims = jwtService.getClaims(refreshToken);
        UUID sessionId = UUID.fromString((String) refreshTokenClaims.get("session_id"));
        Instant now = Instant.now();

        RefreshToken token = new RefreshToken();
        token.setUserId(userId);
        token.setSessionId(sessionId);
        token.setTokenHash(DigestUtils.sha256Hex(refreshToken));
        token.setCreatedAt(Date.from(now));
        token.setExpiryDate(Date.from(now.plus(15, ChronoUnit.MINUTES)));
        repository.save(token);
    }

    @Override
    public void updateToken(UUID userId, UUID sessionId, String refreshToken) {
        repository.updateTokenHash(userId, sessionId, DigestUtils.sha256Hex(refreshToken));
    }

    @Transactional
    @Override
    public void deleteByUserIdAndSessionId(UUID userId, UUID sessionId) {
        repository.deleteByUserIdAndSessionId(userId, sessionId);
    }

    @Transactional
    @Override
    public void deleteByUserId(UUID userId) {
        repository.deleteByUserId(userId);
    }

    @Transactional
    @Override
    public void deleteAll(Collection<RefreshToken> tokensToDelete) {
        repository.deleteAllById(tokensToDelete.stream().map(RefreshToken::getSessionId).toList());
    }
}
