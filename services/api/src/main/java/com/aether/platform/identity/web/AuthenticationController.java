package com.aether.platform.identity.web;

import com.aether.platform.identity.application.InvalidCredentialsException;
import com.aether.platform.identity.application.InvalidRefreshTokenException;
import com.aether.platform.identity.application.SessionAuthenticationService;
import com.aether.platform.identity.domain.AuthenticatedUserPrincipal;
import com.aether.platform.identity.domain.SessionTokens;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final SessionAuthenticationService authenticationService;
    private final Clock clock;

    public AuthenticationController(
            SessionAuthenticationService authenticationService,
            Clock clock) {
        this.authenticationService = authenticationService;
        this.clock = clock;
    }

    @PostMapping("/login")
    public ResponseEntity<SessionTokenResponse> login(
            @Valid @RequestBody LoginRequest request) {

        SessionTokens tokens = authenticationService
                .login(request.email(), request.password())
                .orElseThrow(InvalidCredentialsException::new);

        return ResponseEntity.ok(SessionTokenResponse.from(tokens, clock));
    }

    @PostMapping("/refresh")
    public ResponseEntity<SessionTokenResponse> refresh(
            @Valid @RequestBody RefreshSessionRequest request) {

        SessionTokens tokens = authenticationService
                .refresh(request.refreshToken())
                .orElseThrow(InvalidRefreshTokenException::new);

        return ResponseEntity.ok(SessionTokenResponse.from(tokens, clock));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {

        authenticationService.logout(principal);

        return ResponseEntity.noContent().build();
    }
}