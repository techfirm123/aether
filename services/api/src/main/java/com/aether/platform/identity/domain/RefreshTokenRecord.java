package com.aether.platform.identity.domain;

import java.time.Instant;
import java.util.UUID;

public record RefreshTokenRecord(
        UUID id,
        UUID sessionId,
        Instant expiresAt,
        Instant consumedAt) {
}