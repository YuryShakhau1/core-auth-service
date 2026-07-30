package by.shakhau.ps.auth.controller;

import by.shakhau.ps.auth.config.SecurityProps;
import by.shakhau.ps.auth.controller.dto.request.LoginRequest;
import by.shakhau.ps.auth.controller.dto.request.RefreshTokenRequest;
import by.shakhau.ps.auth.controller.dto.response.TokenResponse;
import by.shakhau.ps.auth.controller.dto.response.TokenValidResponse;
import by.shakhau.ps.auth.controller.exception.UnauthorizedException;
import by.shakhau.ps.auth.controller.filter.JwtAuthenticationFilter.UserPrincipal;
import by.shakhau.ps.auth.model.RefreshToken;
import by.shakhau.ps.auth.model.UserShortCredential;
import by.shakhau.ps.auth.service.RefreshTokenService;
import by.shakhau.ps.auth.service.UserCredentialService;
import by.shakhau.ps.auth.service.exception.ResourceForbiddenException;
import by.shakhau.ps.auth.service.impl.JwtService;
import by.shakhau.ps.auth.service.impl.JwtService.TokenInfo;
import by.shakhau.ps.auth.util.PasswordUtil;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final SecurityProps securityProps;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserCredentialService userCredentialService;

    @GetMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        UserShortCredential userCredential = userCredentialService.findByEmail(request.getEmail());

        var password = new StringBuilder().append(request.getPassword());
        if (userCredential == null || !passwordEncoder.matches(password, userCredential.getPasswordHash())) {
            PasswordUtil.clearPassword(request, password);
            throw new UnauthorizedException("Wrong email or password");
        }

        if (!userCredential.getPasswordActive()) {
            throw new ResourceForbiddenException(
                    "Password is not active. Change password first. Use POST /auth/users/change-password");
        }

        UUID userId = userCredential.getUserId();
        PasswordUtil.clearPassword(request, password);

        TokenInfo accessToken = jwtService.generateAccessToken(userId);
        String refreshToken = jwtService.generateRefreshToken(userId, accessToken.getSessionId());

        refreshTokenService.save(userId, refreshToken);

        deleteSessionOutOfLimit(userId);

        return ResponseEntity.ok(new TokenResponse(accessToken.getToken(), refreshToken));
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new UnauthorizedException("Refresh token %s is invalid".formatted(refreshToken));
        }

        Claims claims = jwtService.getClaims(refreshToken);
        UUID userId = UUID.fromString(claims.getSubject());
        UUID sessionId = UUID.fromString((String) claims.get("session_id"));

        String existingTokenHash = refreshTokenService.findTokenHashByUserIdAndSessionId(userId, sessionId);
        if (existingTokenHash == null) {
            throw new UnauthorizedException("Refresh token %s not found".formatted(refreshToken));
        }

        String refreshTokenHash = DigestUtils.sha256Hex(refreshToken);
        if (!refreshTokenHash.equals(existingTokenHash)) {
            throw new UnauthorizedException("Refresh token %s is not valid".formatted(refreshToken));
        }

        var sId = sessionId.toString();
        TokenInfo generatedAccessToken = jwtService.generateAccessToken(userId, sId);
        String generatedRefreshToken = jwtService.generateRefreshToken(userId, sId);

        refreshTokenService.updateToken(userId, sessionId, generatedRefreshToken);

        return ResponseEntity.ok(new TokenResponse(generatedAccessToken.getToken(), generatedRefreshToken));
    }

    @GetMapping(value = "/token/{token}/valid")
    public ResponseEntity<TokenValidResponse> tokenValid(@PathVariable String token) {
        return ResponseEntity.ok(new TokenValidResponse(jwtService.isTokenValid(token)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserPrincipal principal) {
        refreshTokenService.deleteByUserIdAndSessionId(principal.getId(), principal.getSessionId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout/all")
    public ResponseEntity<Void> logoutAll(@AuthenticationPrincipal UserPrincipal principal) {
        refreshTokenService.deleteByUserId(principal.getId());
        return ResponseEntity.noContent().build();
    }

    private void deleteSessionOutOfLimit(UUID userId) {
        long sessionCount = refreshTokenService.countByUserId(userId);
        int maxSessionCount = securityProps.getMaxSessionCount();

        if (sessionCount > maxSessionCount) {
            List<RefreshToken> tokens = refreshTokenService.findByUserId(userId);
            if (tokens.size() > maxSessionCount) {
                var dateNow = new Date();
                tokens.sort(Comparator.comparing(RefreshToken::getCreatedAt).reversed());
                int fromDeleteIndex = 0;
                while (fromDeleteIndex < tokens.size()) {
                    if (fromDeleteIndex >= maxSessionCount || tokens.get(fromDeleteIndex).getExpiryDate().before(dateNow)) {
                        break;
                    }

                    fromDeleteIndex++;
                }

                if (fromDeleteIndex < tokens.size()) {
                    refreshTokenService.deleteAll(tokens.subList(fromDeleteIndex, tokens.size()));
                }
            }
        }
    }
}
