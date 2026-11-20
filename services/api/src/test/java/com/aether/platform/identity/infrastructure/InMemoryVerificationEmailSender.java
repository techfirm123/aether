package com.aether.platform.identity.infrastructure;

import com.aether.platform.identity.application.VerificationEmailSender;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryVerificationEmailSender implements VerificationEmailSender {

    private final Map<String, String> latestTokensByEmail = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> deliveriesByEmail = new ConcurrentHashMap<>();

    @Override
    public void sendVerificationToken(String emailAddress, String displayName, String token) {
        latestTokensByEmail.put(emailAddress, token);
        deliveriesByEmail.computeIfAbsent(emailAddress, ignored -> new AtomicInteger())
                .incrementAndGet();
    }

    public String latestTokenFor(String emailAddress) {
        return latestTokensByEmail.get(emailAddress);
    }

    public int deliveryCountFor(String emailAddress) {
        AtomicInteger count = deliveriesByEmail.get(emailAddress);
        return count == null ? 0 : count.get();
    }
}