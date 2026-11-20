package com.aether.platform.identity;

import com.aether.platform.identity.infrastructure.InMemoryVerificationEmailSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(AuthenticationIntegrationTests.TestEmailConfiguration.class)
class AuthenticationIntegrationTests {

    private static final String PASSWORD = "Correct-Horse-Battery-42";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private InMemoryVerificationEmailSender emailSender;

    @Test
    void currentUserEndpointRequiresAuthentication() throws Exception {

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unverifiedAccountCannotLogin() throws Exception {

        String email = uniqueEmail();
        register(email);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void verifiedUserCanLoginAndAccessCurrentUser() throws Exception {

        String email = uniqueEmail();
        registerAndVerify(email);

        var tokens = login(email, PASSWORD);

        String accessToken = tokens.get("accessToken").asText();
        String refreshToken = tokens.get("refreshToken").asText();

        assertThat(accessToken).matches("^aet_[A-Za-z0-9_-]{43}$");
        assertThat(refreshToken).matches("^aer_[A-Za-z0-9_-]{43}$");
        assertThat(tokens.get("tokenType").asText()).isEqualTo("Bearer");
        assertThat(tokens.get("expiresInSeconds").asLong()).isPositive();

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.displayName").value("AETHER Test User"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        String accessHash = jdbcTemplate.queryForObject("""
                SELECT user_session.access_token_hash
                FROM aether.user_session user_session
                JOIN aether.user_account user_account
                    ON user_account.id = user_session.user_id
                WHERE user_account.email = ?
                ORDER BY user_session.created_at DESC
                LIMIT 1
                """, String.class, email);

        String refreshHash = jdbcTemplate.queryForObject("""
                SELECT user_refresh_token.token_hash
                FROM aether.user_refresh_token user_refresh_token
                JOIN aether.user_session user_session
                    ON user_session.id = user_refresh_token.session_id
                JOIN aether.user_account user_account
                    ON user_account.id = user_session.user_id
                WHERE user_account.email = ?
                ORDER BY user_refresh_token.issued_at DESC
                LIMIT 1
                """, String.class, email);

        assertThat(accessHash).hasSize(64).isNotEqualTo(accessToken);
        assertThat(refreshHash).hasSize(64).isNotEqualTo(refreshToken);
    }

    @Test
    void incorrectPasswordIsRejected() throws Exception {

        String email = uniqueEmail();
        registerAndVerify(email);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, "Incorrect-Password-Value-99")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void refreshRotatesTokensAndReplayRevokesSession() throws Exception {

        String email = uniqueEmail();
        registerAndVerify(email);

        var originalTokens = login(email, PASSWORD);

        String originalAccessToken =
                originalTokens.get("accessToken").asText();

        String originalRefreshToken =
                originalTokens.get("refreshToken").asText();

        MvcResult refreshResult = mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"refreshToken":"%s"}
                                        """.formatted(originalRefreshToken)))
                .andExpect(status().isOk())
                .andReturn();

        var rotatedTokens = jsonMapper.readTree(
                refreshResult.getResponse().getContentAsString());

        String rotatedAccessToken =
                rotatedTokens.get("accessToken").asText();

        String rotatedRefreshToken =
                rotatedTokens.get("refreshToken").asText();

        assertThat(rotatedAccessToken).isNotEqualTo(originalAccessToken);
        assertThat(rotatedRefreshToken).isNotEqualTo(originalRefreshToken);

        // Previous access token is invalid after rotation.
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + originalAccessToken))
                .andExpect(status().isUnauthorized());

        // New access token works before replay detection.
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + rotatedAccessToken))
                .andExpect(status().isOk());

        // Reuse of the original refresh token must be rejected.
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"%s"}
                                """.formatted(originalRefreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));

        // Replay detection revokes the entire session.
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + rotatedAccessToken))
                .andExpect(status().isUnauthorized());

        Integer replayEvents = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM aether.identity_audit_event audit_event
                JOIN aether.user_account user_account
                    ON user_account.id = audit_event.user_id
                WHERE user_account.email = ?
                  AND audit_event.event_type = 'REFRESH_TOKEN_REUSE_DETECTED'
                """, Integer.class, email);

        assertThat(replayEvents).isEqualTo(1);
    }

    @Test
    void logoutRevokesCurrentSession() throws Exception {

        String email = uniqueEmail();
        registerAndVerify(email);

        var tokens = login(email, PASSWORD);
        String accessToken = tokens.get("accessToken").asText();

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());

        Integer revokedEvents = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM aether.identity_audit_event audit_event
                JOIN aether.user_account user_account
                    ON user_account.id = audit_event.user_id
                WHERE user_account.email = ?
                  AND audit_event.event_type = 'SESSION_REVOKED'
                """, Integer.class, email);

        assertThat(revokedEvents).isEqualTo(1);
    }

    @Test
    void eachAuthenticatedUserOnlySeesTheirOwnIdentity() throws Exception {

        String firstEmail = uniqueEmail();
        String secondEmail = uniqueEmail();

        registerAndVerify(firstEmail);
        registerAndVerify(secondEmail);

        var firstTokens = login(firstEmail, PASSWORD);
        var secondTokens = login(secondEmail, PASSWORD);

        String firstAccessToken = firstTokens.get("accessToken").asText();
        String secondAccessToken = secondTokens.get("accessToken").asText();

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + firstAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(firstEmail));

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + secondAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(secondEmail));
    }

    @Test
    void authenticatedUnknownApiRouteReturnsNotFound() throws Exception {

        String email = uniqueEmail();
        registerAndVerify(email);

        var tokens = login(email, PASSWORD);
        String accessToken = tokens.get("accessToken").asText();

        mockMvc.perform(get("/api/v1/does-not-exist")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    private void register(String email) throws Exception {

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "displayName": "AETHER Test User",
                                  "password": "%s"
                                }
                                """.formatted(email, PASSWORD)))
                .andExpect(status().isAccepted());
    }

    private void registerAndVerify(String email) throws Exception {

        register(email);

        String token = emailSender.latestTokenFor(email);
        assertThat(token).isNotBlank();

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s"}
                                """.formatted(token)))
                .andExpect(status().isNoContent());
    }

    private tools.jackson.databind.JsonNode login(
            String email,
            String password) throws Exception {

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, password)))
                .andExpect(status().isOk())
                .andReturn();

        return jsonMapper.readTree(
                result.getResponse().getContentAsString());
    }

    private String loginBody(String email, String password) {

        return """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);
    }

    private String uniqueEmail() {
        return "aether-auth-" + UUID.randomUUID() + "@example.com";
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestEmailConfiguration {

        @Bean
        InMemoryVerificationEmailSender verificationEmailSender() {
            return new InMemoryVerificationEmailSender();
        }
    }
}