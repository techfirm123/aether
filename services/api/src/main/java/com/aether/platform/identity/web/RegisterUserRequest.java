package com.aether.platform.identity.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @NotBlank
        @Email
        @Size(max = 320)
        String email,

        @NotBlank
        @Size(min = 2, max = 80)
        String displayName,

        @NotBlank
        @Size(min = 12, max = 128)
        String password) {
}