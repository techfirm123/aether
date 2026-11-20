package com.aether.platform.identity.domain;

import java.security.Principal;
import java.util.UUID;

public record AuthenticatedUserPrincipal(
        UUID userId,
        UUID sessionId,
        String email,
        String displayName) implements Principal {

    @Override
    public String getName() {
        return email;
    }
}