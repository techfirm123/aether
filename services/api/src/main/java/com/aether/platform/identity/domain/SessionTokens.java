package com.aether.platform.identity.domain;

import java.time.Instant;

public record SessionTokens(
        String accessToken,
        String refreshToken,
        Instant accessExpiresAt,
        Instant refreshExpiresAt) {
}