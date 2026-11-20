package com.aether.platform.identity.domain;

import java.time.Instant;
import java.util.UUID;

public record EmailVerificationToken(
        UUID id,
        UUID userId,
        Instant expiresAt) {
}