package by.shakhau.ps.auth.integration;

import by.shakhau.ps.auth.controller.dto.request.LoginRequest;
import by.shakhau.ps.auth.controller.dto.request.RefreshTokenRequest;
import by.shakhau.ps.auth.model.UserCredential;
import by.shakhau.ps.auth.service.RefreshTokenService;
import by.shakhau.ps.auth.service.impl.JwtService.TokenInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.shaded.com.google.common.net.HttpHeaders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIT extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @Test
    void shouldLogin() throws Exception {
        UserCredential user = createUser();

        when(jwtService.generateAccessToken(user.getUserId())).thenReturn(
                new TokenInfo("access-token", UUID.randomUUID().toString()));
        when(jwtService.generateRefreshToken(eq(user.getUserId()), any()))
                .thenReturn("refresh-token");

        LoginRequest request = new LoginRequest(
                user.getEmail(),
                new StringBuilder("Password1!"));

        mockMvc.perform(get("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(refreshTokenService).save(user.getUserId(), "refresh-token");
    }

    @Test
    void shouldReturnUnauthorizedWhenPasswordIsWrong() throws Exception {
        UserCredential user = createUser();

        LoginRequest request = new LoginRequest(
                user.getEmail(),
                new StringBuilder("WrongPassword"));

        mockMvc.perform(get("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRefreshToken() throws Exception {
        UUID userId = UUID.fromString(USER_ID);
        UUID sessionId = UUID.randomUUID();

        Claims claims = jwtService.getClaims("refresh-token");
        when(claims.get("session_id")).thenReturn(sessionId.toString());

        when(jwtService.isTokenValid("refresh-token")).thenReturn(true);

        when(refreshTokenService.findTokenHashByUserIdAndSessionId(userId, sessionId))
                .thenReturn(DigestUtils.sha256Hex("refresh-token"));
        when(jwtService.generateAccessToken(userId, sessionId.toString())).thenReturn(
                new TokenInfo("new-access", sessionId.toString()));
        when(jwtService.generateRefreshToken(userId, sessionId.toString())).thenReturn("new-refresh");

        var request = new RefreshTokenRequest("refresh-token");

        mockMvc.perform(post("/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));

        verify(refreshTokenService)
                .updateToken(userId, sessionId, "new-refresh");
    }

    @Test
    void shouldValidateToken() throws Exception {
        when(jwtService.isTokenValid("token")).thenReturn(true);

        mockMvc.perform(get("/auth/token/{token}/valid", "token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value("true"));
    }

    @Test
    void shouldLogout() throws Exception {
        UUID userId = UUID.fromString(USER_ID);
        UUID sessionId = UUID.randomUUID();

        Claims claims = jwtService.getClaims("access-token");
        when(claims.get("session_id")).thenReturn(sessionId.toString());

        mockMvc.perform(post("/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isNoContent());

        verify(refreshTokenService)
                .deleteByUserIdAndSessionId(userId, sessionId);
    }

    @Test
    void shouldLogoutAll() throws Exception {
        UUID userId = UUID.fromString(USER_ID);

        mockMvc.perform(post("/auth/logout/all")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isNoContent());

        verify(refreshTokenService).deleteByUserId(userId);
    }
}
