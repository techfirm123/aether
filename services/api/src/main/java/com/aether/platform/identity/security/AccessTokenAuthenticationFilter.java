package com.aether.platform.identity.security;

import com.aether.platform.identity.application.SessionAuthenticationService;
import com.aether.platform.identity.domain.AuthenticatedUserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class AccessTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final SessionAuthenticationService authenticationService;

    public AccessTokenAuthenticationFilter(
            SessionAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader(AUTHORIZATION_HEADER);

        if (authorizationHeader != null
                && authorizationHeader.regionMatches(
                true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {

            String rawToken = authorizationHeader
                    .substring(BEARER_PREFIX.length())
                    .strip();

            Optional<AuthenticatedUserPrincipal> principalOptional =
                    authenticationService.authenticateAccessToken(rawToken);

            principalOptional.ifPresent(principal -> {

                if (SecurityContextHolder.getContext().getAuthentication() == null) {

                    UsernamePasswordAuthenticationToken authentication =
                            UsernamePasswordAuthenticationToken.authenticated(
                                    principal,
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_USER")));

                    SecurityContext securityContext =
                            SecurityContextHolder.createEmptyContext();

                    securityContext.setAuthentication(authentication);
                    SecurityContextHolder.setContext(securityContext);
                }
            });
        }

        filterChain.doFilter(request, response);
    }
}