package com.aether.platform.identity.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RefreshSessionRequest(
        @NotBlank
        @Pattern(regexp = "^aer_[A-Za-z0-9_-]{43}$")
        String refreshToken) {
}