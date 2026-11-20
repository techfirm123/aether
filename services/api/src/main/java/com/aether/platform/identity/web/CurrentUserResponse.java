package com.aether.platform.identity.web;

import com.aether.platform.identity.domain.AuthenticatedUserPrincipal;

import java.util.UUID;

public record CurrentUserResponse(
        UUID id,
        String email,
        String displayName,
        String status) {

    public static CurrentUserResponse from(
            AuthenticatedUserPrincipal principal) {

        return new CurrentUserResponse(
                principal.userId(),
                principal.email(),
                principal.displayName(),
                "ACTIVE");
    }
}