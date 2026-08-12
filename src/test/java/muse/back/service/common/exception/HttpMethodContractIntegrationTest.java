package muse.back.service.common.exception;

import auth.common.core.context.RequirePrincipalRoleFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class HttpMethodContractIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FilterRegistrationBean<RequirePrincipalRoleFilter> requirePrincipalRoleFilter;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(requirePrincipalRoleFilter.getFilter())
                .build();
    }

    @Test
    void existingGetEndpoint_post_returnsWrappedMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/api/muse/v1/profile/summary")
                        .header("X-User-Key", "muse-user-key")
                        .header("X-User-Role", "USER"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(4050000));
    }

    @Test
    void missingEndpoint_get_returnsWrappedNotFound() throws Exception {
        mockMvc.perform(get("/api/muse/v1/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(4040000));
    }

    @Test
    void submitEntry_invalidPayload_returnsWrappedValidationError() throws Exception {
        mockMvc.perform(post("/api/muse/v1/contests/101/entries")
                        .header("X-User-Key", "muse-user-key")
                        .header("X-User-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "description": "invalid dimensions",
                                  "fileName": "sample.jpg",
                                  "fileSizeBytes": 1024,
                                  "imageWidthPx": 1200,
                                  "imageHeightPx": 800
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(4000300));
    }

    @Test
    void paymentWebhook_missingData_returnsValidationError() throws Exception {
        mockMvc.perform(post("/api/muse/v1/payments/webhooks/toss")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
