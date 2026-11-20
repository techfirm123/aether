package com.aether.platform.identity;

import com.aether.platform.identity.infrastructure.InMemoryVerificationEmailSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(IdentityRegistrationIntegrationTests.TestEmailConfiguration.class)
class IdentityRegistrationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private InMemoryVerificationEmailSender emailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registrationCreatesPendingAccountAndStoresAdaptivePasswordHash() throws Exception {
        String email = uniqueEmail();
        String password = "Correct-Horse-Battery-42";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationBody(email.toUpperCase(Locale.ROOT), password)))
                .andExpect(status().isAccepted());

        Map<String, Object> account = jdbcTemplate.queryForMap("""
                SELECT id, email, display_name, status
                FROM aether.user_account
                WHERE email = ?
                """, email);

        String storedHash = jdbcTemplate.queryForObject("""
                SELECT password_hash
                FROM aether.user_password_credential
                WHERE user_id = ?
                """, String.class, account.get("id"));

        assertThat(account.get("status")).isEqualTo("PENDING_VERIFICATION");
        assertThat(storedHash).isNotEqualTo(password);
        assertThat(passwordEncoder.matches(password, storedHash)).isTrue();

        String rawVerificationToken = emailSender.latestTokenFor(email);
        assertThat(rawVerificationToken).matches("^[A-Za-z0-9_-]{43}$");

        String storedTokenHash = jdbcTemplate.queryForObject("""
                SELECT token_hash
                FROM aether.email_verification_token
                WHERE user_id = ?
                """, String.class, account.get("id"));

        assertThat(storedTokenHash).hasSize(64).isNotEqualTo(rawVerificationToken);

        Integer auditCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM aether.identity_audit_event
                WHERE user_id = ?
                  AND event_type = 'USER_REGISTRATION_REQUESTED'
                """, Integer.class, account.get("id"));

        assertThat(auditCount).isEqualTo(1);
    }

    @Test
    void verificationActivatesAccountAndConsumesToken() throws Exception {
        String email = uniqueEmail();
        register(email);
        String token = emailSender.latestTokenFor(email);

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s"}
                                """.formatted(token)))
                .andExpect(status().isNoContent());

        Map<String, Object> account = jdbcTemplate.queryForMap("""
                SELECT status, email_verified_at
                FROM aether.user_account
                WHERE email = ?
                """, email);

        assertThat(account.get("status")).isEqualTo("ACTIVE");
        assertThat(account.get("email_verified_at")).isNotNull();

        Integer consumedCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM aether.email_verification_token verification
                JOIN aether.user_account registered_user
                    ON registered_user.id = verification.user_id
                WHERE registered_user.email = ?
                  AND verification.consumed_at IS NOT NULL
                """, Integer.class, email);

        assertThat(consumedCount).isEqualTo(1);
    }

    @Test
    void verificationTokenCannotBeReused() throws Exception {
        String email = uniqueEmail();
        register(email);
        String token = emailSender.latestTokenFor(email);

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s"}
                                """.formatted(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s"}
                                """.formatted(token)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidVerificationTokenIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void expiredVerificationTokenIsRejected() throws Exception {
        String email = uniqueEmail();
        register(email);
        String token = emailSender.latestTokenFor(email);

        jdbcTemplate.update("""
                UPDATE aether.email_verification_token
                SET expires_at = ?
                WHERE user_id = (
                    SELECT id FROM aether.user_account WHERE email = ?
                )
                """,
                Timestamp.from(Instant.now().minusSeconds(60)),
                email);

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s"}
                                """.formatted(token)))
                .andExpect(status().isBadRequest());

        String accountStatus = jdbcTemplate.queryForObject("""
                SELECT status FROM aether.user_account WHERE email = ?
                """, String.class, email);

        assertThat(accountStatus).isEqualTo("PENDING_VERIFICATION");
    }

    @Test
    void duplicateRegistrationDoesNotRevealWhetherEmailAlreadyExists() throws Exception {
        String email = uniqueEmail();
        register(email);
        int originalDeliveryCount = emailSender.deliveryCountFor(email);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationBody(email, "Another-Password-Value-43")))
                .andExpect(status().isAccepted());

        Integer accountCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM aether.user_account WHERE email = ?
                """, Integer.class, email);

        assertThat(accountCount).isEqualTo(1);
        assertThat(emailSender.deliveryCountFor(email)).isEqualTo(originalDeliveryCount);
    }

    @Test
    void registrationRejectsShortPasswords() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationBody(uniqueEmail(), "short")))
                .andExpect(status().isBadRequest());
    }

    private void register(String email) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationBody(email, "Correct-Horse-Battery-42")))
                .andExpect(status().isAccepted());
    }

    private String registrationBody(String email, String password) {
        return """
                {
                  "email": "%s",
                  "displayName": "AETHER Test User",
                  "password": "%s"
                }
                """.formatted(email, password);
    }

    private String uniqueEmail() {
        return "aether-" + UUID.randomUUID() + "@example.com";
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestEmailConfiguration {

        @Bean
        InMemoryVerificationEmailSender verificationEmailSender() {
            return new InMemoryVerificationEmailSender();
        }
    }
}