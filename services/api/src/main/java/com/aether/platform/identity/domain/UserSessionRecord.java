package com.aether.platform.identity.domain;

import java.time.Instant;
import java.util.UUID;

public record UserSessionRecord(
        UUID id,
        UUID userId,
        String email,
        String displayName,
        String accountStatus,
        Instant expiresAt,
        Instant revokedAt) {
}