package com.aether.platform.identity.web;

import com.aether.platform.identity.application.EmailVerificationService;
import com.aether.platform.identity.application.UserRegistrationService;
import jakarta.validation.Valid;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class IdentityController {

    private final UserRegistrationService registrationService;
    private final EmailVerificationService emailVerificationService;

    public IdentityController(
            UserRegistrationService registrationService,
            EmailVerificationService emailVerificationService) {
        this.registrationService = registrationService;
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationAcceptedResponse> register(
            @Valid @RequestBody RegisterUserRequest request) {

        try {
            registrationService.register(
                    request.email(),
                    request.displayName(),
                    request.password());
        } catch (DuplicateKeyException ignored) {
            // A concurrent duplicate registration has the same public
            // response as an existing address to avoid account enumeration.
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(RegistrationAcceptedResponse.standardMessage());
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {

        emailVerificationService.verify(request.token());
        return ResponseEntity.noContent().build();
    }
}