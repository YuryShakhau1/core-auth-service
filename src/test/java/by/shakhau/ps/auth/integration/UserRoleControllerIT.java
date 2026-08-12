package by.shakhau.ps.auth.integration;

import by.shakhau.ps.auth.controller.dto.request.AddUserRolesRequest;
import by.shakhau.ps.auth.service.UserRoleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static by.shakhau.ps.auth.controller.filter.AuthenticationFilter.SESSION_ID_HEADER;
import static by.shakhau.ps.auth.controller.filter.AuthenticationFilter.USER_ID_HEADER;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserRoleControllerIT extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserRoleService userRoleService;

    @Test
    void shouldReturnUserRoles() throws Exception {
        UUID userId = UUID.randomUUID();

        when(userRoleService.findUserRoles(userId)).thenReturn(List.of("ADMIN", "USER"));

        mockMvc.perform(get("/auth/users/{userId}/roles", userId)
                        .header(USER_ID_HEADER, userId)
                        .header(SESSION_ID_HEADER, SESSION_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.roleNames").isArray())
                .andExpect(jsonPath("$.roleNames[0]").value("ADMIN"))
                .andExpect(jsonPath("$.roleNames[1]").value("USER"));

        verify(userRoleService).findUserRoles(userId);
    }

    @Test
    void shouldAddUserRoles() throws Exception {
        UUID userId = UUID.randomUUID();

        AddUserRolesRequest request = new AddUserRolesRequest(List.of("ADMIN", "USER"));

        mockMvc.perform(post("/auth/users/{userId}/roles", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(USER_ID_HEADER, userId)
                        .header(SESSION_ID_HEADER, SESSION_ID)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(userRoleService).addUserRoles(userId, List.of("ADMIN", "USER"));
    }

    @Test
    void shouldDeleteUserRole() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/auth/users/{userId}/roles/{roleName}", userId, "ADMIN")
                        .header(USER_ID_HEADER, userId)
                        .header(SESSION_ID_HEADER, SESSION_ID))
                .andExpect(status().isNoContent());

        verify(userRoleService).deleteUserRole(userId, "ADMIN");
    }
}
