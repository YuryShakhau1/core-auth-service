package by.shakhau.ps.auth.repository;

import by.shakhau.ps.auth.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    List<RefreshToken> findByUserId(UUID userId);

    @Query("SELECT rt.tokenHash FROM RefreshToken rt WHERE rt.userId = :userId AND rt.sessionId = :sessionId")
    Optional<String> findTokenHashByUserIdAndSessionId(UUID userId, UUID sessionId);

    long countByUserId(UUID userId);

    @Query("""
           UPDATE RefreshToken rt SET rt.tokenHash = :tokenHash 
           WHERE rt.userId = :userId AND rt.sessionId = :sessionId
           """)
    @Modifying
    void updateTokenHash(UUID userId, UUID sessionId, String tokenHash);

    @Modifying
    void deleteByUserIdAndSessionId(UUID userId, UUID sessionId);

    @Modifying
    void deleteByUserId(UUID userId);
}
