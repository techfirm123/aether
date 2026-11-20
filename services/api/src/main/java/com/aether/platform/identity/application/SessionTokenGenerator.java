package com.aether.platform.identity.application;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.regex.Pattern;

@Component
public class SessionTokenGenerator {

    private static final int TOKEN_BYTE_LENGTH = 32;

    private static final Pattern ACCESS_TOKEN_PATTERN =
            Pattern.compile("^aet_[A-Za-z0-9_-]{43}$");

    private static final Pattern REFRESH_TOKEN_PATTERN =
            Pattern.compile("^aer_[A-Za-z0-9_-]{43}$");

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateAccessToken() {
        return generateToken("aet_");
    }

    public String generateRefreshToken() {
        return generateToken("aer_");
    }

    private String generateToken(String prefix) {
        byte[] tokenBytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(tokenBytes);

        return prefix + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);
    }

    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available.",
                    exception);
        }
    }

    public boolean isValidAccessTokenFormat(String token) {
        return token != null
                && ACCESS_TOKEN_PATTERN.matcher(token).matches();
    }

    public boolean isValidRefreshTokenFormat(String token) {
        return token != null
                && REFRESH_TOKEN_PATTERN.matcher(token).matches();
    }
}