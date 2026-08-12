package by.shakhau.ps.auth.integration;

import by.shakhau.ps.auth.controller.dto.request.ChangePasswordRequest;
import by.shakhau.ps.auth.controller.dto.request.CreateFirstAdminRequest;
import by.shakhau.ps.auth.controller.dto.request.CreateUserRequest;
import by.shakhau.ps.auth.model.UserCredential;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.UUID;

import static by.shakhau.ps.auth.controller.filter.AuthenticationFilter.SESSION_ID_HEADER;
import static by.shakhau.ps.auth.controller.filter.AuthenticationFilter.USER_ID_HEADER;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserCredentialControllerIT extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldFindCurrentUser() throws Exception {
        UserCredential user = createUser();

        Claims claims = jwtService.getClaims("access-token");
        when(claims.getSubject()).thenReturn(user.getUserId().toString());

        mockMvc.perform(get("/auth/users/me")
                        .header(USER_ID_HEADER, user.getUserId())
                        .header(SESSION_ID_HEADER, SESSION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ivan@test.com"))
                .andExpect(jsonPath("$.firstName").value("Ivan"))
                .andExpect(jsonPath("$.lastName").value("Ivanov"));
    }

    @Test
    void shouldFindCurrentUserRoles() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(get("/auth/users/me/roles")
                        .header(USER_ID_HEADER, userId)
                        .header(SESSION_ID_HEADER, SESSION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleNames").isArray());
    }

    @Test
    void shouldCreateUser() throws Exception {
        var request = new CreateUserRequest();
        request.setFirstName("Petr");
        request.setLastName("Petrov");
        request.setBirthDate(LocalDate.of(1990, 1, 1));
        request.setEmail("petr@test.com");
        request.setPassword(new StringBuilder("Password1!"));
        request.setRepeatPassword(new StringBuilder("Password1!"));
        request.setActive(true);

        mockMvc.perform(post("/auth/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email")
                        .value("petr@test.com"));
    }

    @Test
    void shouldCreateAdmin() throws Exception {
        when(securityProps.getAdminInitSecretHash()).thenReturn(ADMIN_SECRET_HASH);

        var request = new CreateFirstAdminRequest();

        request.setFirstName("Admin");
        request.setLastName("Admin");
        request.setBirthDate(LocalDate.of(1990, 1, 1));
        request.setEmail("admin@test.com");
        request.setPassword(new StringBuilder("Password1!"));
        request.setRepeatPassword(new StringBuilder("Password1!"));
        request.setActive(true);
        request.setAdminInitSecret(ADMIN_SECRET);

        mockMvc.perform(post("/auth/users/create-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email")
                        .value("admin@test.com"));
    }

    @Test
    void shouldRejectCreateAdminWithWrongSecret() throws Exception {
        when(securityProps.getAdminInitSecretHash()).thenReturn("wrong-hash");

        var request = new CreateFirstAdminRequest();

        request.setFirstName("Admin");
        request.setLastName("Admin");
        request.setBirthDate(LocalDate.of(1990, 1, 1));
        request.setEmail("admin@test.com");
        request.setPassword(new StringBuilder("Password1!"));
        request.setRepeatPassword(new StringBuilder("Password1!"));
        request.setActive(true);
        request.setAdminInitSecret("wrong-secret");

        mockMvc.perform(post("/auth/users/create-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldChangePassword() throws Exception {
        UserCredential user = createUser();

        var password = new StringBuilder("Password1!");

        var request = new ChangePasswordRequest();
        request.setEmail(user.getEmail());
        request.setPassword(password);
        request.setRepeatPassword(password);
        request.setNewPassword(new StringBuilder("NewPassword1!"));

        mockMvc.perform(patch("/auth/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectChangePasswordWhenPasswordsDoNotMatch() throws Exception {
        var request = new ChangePasswordRequest();
        request.setEmail("ivan@test.com");
        request.setPassword(new StringBuilder("Password1!"));
        request.setRepeatPassword(new StringBuilder("Different1!"));
        request.setNewPassword(new StringBuilder("NewPassword1!"));

        mockMvc.perform(patch("/auth/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
