package by.shakhau.ps.auth.integration;

import by.shakhau.ps.auth.controller.dto.request.AddUserRolesRequest;
import by.shakhau.ps.auth.model.UserCredential;
import by.shakhau.ps.auth.service.UserRoleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.UUID;

import static by.shakhau.ps.auth.controller.filter.AuthenticationFilter.SESSION_ID_HEADER;
import static by.shakhau.ps.auth.controller.filter.AuthenticationFilter.USER_ID_HEADER;
import static by.shakhau.ps.auth.model.Role.ROLE_ADMIN;
import static by.shakhau.ps.auth.model.Role.ROLE_USER;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserRoleControllerIT extends AbstractIntegrationTest {

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnUserRoles() throws Exception {
        UserCredential user = createUser();
        UUID userId = user.getUserId();

        userRoleService.addUserRole(userId, ROLE_USER);

        mockMvc.perform(get("/auth/users/roles")
                        .header(SESSION_ID_HEADER, SESSION_ID)
                        .param(USER_ID_PARAM, userId.toString())
                        .param(USER_ID_HEADER, userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.roleNames").isArray())
                .andExpect(jsonPath("$.roleNames[0]").value(ROLE_ADMIN))
                .andExpect(jsonPath("$.roleNames[1]").value(ROLE_USER));
    }

    @Test
    void shouldUpdateUserRoles() throws Exception {
        UserCredential user = createUser();
        UUID userId = user.getUserId();

        var request = new AddUserRolesRequest(List.of(ROLE_USER));

        mockMvc.perform(patch("/auth/users/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param(USER_ID_HEADER, userId.toString())
                        .param(USER_ID_PARAM, userId.toString())
                        .header(SESSION_ID_HEADER, SESSION_ID)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        List<String> userRoleNames = userRoleService.findUserRoleNames(userId);

        Assertions.assertEquals(List.of(ROLE_USER), userRoleNames);
    }

    @Test
    void shouldDeleteUserRole() throws Exception {
        UserCredential user = createUser();
        UUID userId = user.getUserId();

        mockMvc.perform(delete("/auth/users/roles/{roleName}", ROLE_ADMIN)
                        .param(USER_ID_HEADER, userId.toString())
                        .param(USER_ID_PARAM, userId.toString())
                        .header(SESSION_ID_HEADER, SESSION_ID))
                .andExpect(status().isNoContent());

        assertTrue(userRoleService.findUserRoleNames(userId).isEmpty());
    }

    @Test
    void shouldFindCurrentUserRoles() throws Exception {
        UserCredential user = createUser();
        UUID userId = user.getUserId();

        mockMvc.perform(get("/auth/users/roles/me")
                        .header(USER_ID_HEADER, userId)
                        .header(SESSION_ID_HEADER, SESSION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleNames").isArray())
                .andExpect(jsonPath("$.roleNames[0]").value("ROLE_ADMIN"));
    }
}
