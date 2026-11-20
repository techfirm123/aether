package com.aether.platform.identity.web;

import com.aether.platform.identity.domain.AuthenticatedUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class CurrentUserController {

    @GetMapping("/me")
    public CurrentUserResponse currentUser(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {

        return CurrentUserResponse.from(principal);
    }
}