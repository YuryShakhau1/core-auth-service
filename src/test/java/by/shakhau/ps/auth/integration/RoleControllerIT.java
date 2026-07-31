package by.shakhau.ps.auth.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RoleControllerIT extends AbstractIntegrationTest {

    @Test
    void shouldReturnAllRoles() throws Exception {
        mockMvc.perform(get("/auth/roles"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.roleNames").isArray())
                .andExpect(jsonPath("$.roleNames").isNotEmpty());
    }
}
