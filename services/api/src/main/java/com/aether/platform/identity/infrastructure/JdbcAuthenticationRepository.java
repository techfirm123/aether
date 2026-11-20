package com.aether.platform.identity.infrastructure;

import com.aether.platform.identity.domain.LoginAccount;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcAuthenticationRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAuthenticationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<LoginAccount> findAccountForLogin(String normalizedEmail) {

        List<LoginAccount> accounts = jdbcTemplate.query("""
                SELECT
                    user_account.id AS user_id,
                    user_account.email,
                    user_account.display_name,
                    user_account.status AS account_status,
                    user_password_credential.password_hash
                FROM aether.user_account user_account
                JOIN aether.user_password_credential user_password_credential
                    ON user_password_credential.user_id = user_account.id
                WHERE user_account.email = ?
                """,
                (resultSet, rowNumber) -> new LoginAccount(
                        resultSet.getObject("user_id", java.util.UUID.class),
                        resultSet.getString("email"),
                        resultSet.getString("display_name"),
                        resultSet.getString("account_status"),
                        resultSet.getString("password_hash")),
                normalizedEmail);

        return accounts.stream().findFirst();
    }
}