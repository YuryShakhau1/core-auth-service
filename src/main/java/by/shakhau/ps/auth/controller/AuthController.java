package by.shakhau.ps.auth.controller;

import by.shakhau.ps.auth.config.SecurityProps;
import by.shakhau.ps.auth.controller.dto.request.LoginRequest;
import by.shakhau.ps.auth.controller.dto.response.TokenResponse;
import by.shakhau.ps.auth.controller.exception.UnauthorizedException;
import by.shakhau.ps.auth.controller.filter.AuthenticationFilter.UserPrincipal;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private static final String METHOD_REFRESH_TOKEN_URL = "/token/refresh";
    public static final String REFRESH_TOKEN_URL = "/auth" + METHOD_REFRESH_TOKEN_URL;

    private final SecurityProps securityProps;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserCredentialService userCredentialService;

    @PostMapping(value = "/login", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
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

        return buildTokenResponse(accessToken.getToken(), refreshToken);
    }

    @PostMapping(value = METHOD_REFRESH_TOKEN_URL, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenResponse> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new UnauthorizedException("Refresh token is invalid");
        }

        Claims claims = jwtService.getClaims(refreshToken);
        UUID userId = UUID.fromString(claims.getSubject());
        UUID sessionId = UUID.fromString((String) claims.get("session_id"));

        String existingTokenHash = refreshTokenService.findTokenHashByUserIdAndSessionId(userId, sessionId);
        if (existingTokenHash == null) {
            throw new UnauthorizedException("Refresh token not found");
        }

        String refreshTokenHash = DigestUtils.sha256Hex(refreshToken);
        if (!refreshTokenHash.equals(existingTokenHash)) {
            throw new UnauthorizedException("Refresh token is not valid");
        }

        var sId = sessionId.toString();
        TokenInfo generatedAccessToken = jwtService.generateAccessToken(userId, sId);
        String generatedRefreshToken = jwtService.generateRefreshToken(userId, sId);

        refreshTokenService.updateToken(userId, sessionId, generatedRefreshToken);

        return buildTokenResponse(generatedAccessToken.getToken(), refreshToken);
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

    private ResponseEntity<TokenResponse> buildTokenResponse(String accessToken, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path(REFRESH_TOKEN_URL)
                .maxAge(securityProps.getRefreshExpiration() * 1000 + 60 * 1000)
                .build();

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new TokenResponse(accessToken));
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
