package by.shakhau.ps.auth.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PublicKeyControllerIT extends AbstractIntegrationTest {

    @Test
    void shouldReturnPublicKeyAsPlainText() throws Exception {
        mockMvc.perform(get("/auth/public-key"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string(containsString(PUBLIC_KEY)));
    }
}
