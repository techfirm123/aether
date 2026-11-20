package com.aether.platform.identity.domain;

import java.util.UUID;

public record LoginAccount(
        UUID userId,
        String email,
        String displayName,
        String accountStatus,
        String passwordHash) {
}