package com.aether.platform.identity.web;

public record RegistrationAcceptedResponse(String message) {

    public static RegistrationAcceptedResponse standardMessage() {
        return new RegistrationAcceptedResponse(
                "If this address can be registered, verification instructions will be sent.");
    }
}