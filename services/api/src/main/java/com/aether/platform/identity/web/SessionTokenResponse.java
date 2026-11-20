package com.aether.platform.identity.web;

import com.aether.platform.identity.domain.SessionTokens;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public record SessionTokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        Instant refreshExpiresAt) {

    public static SessionTokenResponse from(
            SessionTokens tokens,
            Clock clock) {

        long expiresInSeconds = Math.max(
                0,
                Duration.between(
                        clock.instant(),
                        tokens.accessExpiresAt()).toSeconds());

        return new SessionTokenResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                "Bearer",
                expiresInSeconds,
                tokens.refreshExpiresAt());
    }
}