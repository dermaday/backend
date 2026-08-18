package com.likelion.dermaday.api.auth.security;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "management.endpoint.health.probes.enabled=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void permitsKubernetesHealthProbesWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk());
    }

    @Test
    void returnsCsrfTokenAndAllowsConfiguredFrontendOrigin() throws Exception {
        mockMvc.perform(get("/api/v1/auth/csrf")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"))
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andExpect(jsonPath("$.data.headerName").value("X-XSRF-TOKEN"))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void servesOpenApiDocumentationEvenWithInvalidAccessCookie() throws Exception {
        mockMvc.perform(get("/v3/api-docs")
                        .cookie(new Cookie("DERMADAY_ACCESS_TOKEN", "invalid-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Dermaday API"))
                .andExpect(jsonPath("$.components.securitySchemes.cookieAuth.type").value("apiKey"))
                .andExpect(jsonPath("$.components.securitySchemes.cookieAuth.in").value("cookie"))
                .andExpect(jsonPath("$.components.securitySchemes.cookieAuth.name")
                        .value("DERMADAY_ACCESS_TOKEN"));
    }

    @Test
    void servesSwaggerUi() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/swagger-ui/swagger-initializer.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("withCredentials")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("XSRF-TOKEN")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("X-XSRF-TOKEN")));
    }

    @Test
    void rejectsProtectedMemberApiWithoutAccessToken() throws Exception {
        mockMvc.perform(get("/api/v1/members/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void requiresCsrfTokenForLogout() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isForbidden());
    }

    @Test
    void logsOutWithCsrfTokenAndClearsAccessCookie() throws Exception {
        MvcResult csrfResult = mockMvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();
        Cookie csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");
        String csrfToken = JsonTestSupport.readDataToken(csrfResult.getResponse().getContentAsString());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfToken))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.allOf(
                                org.hamcrest.Matchers.containsString("DERMADAY_ACCESS_TOKEN="),
                                org.hamcrest.Matchers.containsString("Max-Age=0")
                        )
                ));
    }
}
