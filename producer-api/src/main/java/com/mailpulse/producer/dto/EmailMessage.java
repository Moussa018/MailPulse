package com.mailpulse.producer.dto;

import java.time.Instant;
import java.util.UUID;

public record EmailMessage(
        UUID id,
        String to,
        String subject,
        String body,
        Instant createdAt
) {
    public static EmailMessage from(NotifyRequest request) {
        return new EmailMessage(UUID.randomUUID(), request.to(), request.subject(), request.body(), Instant.now());
    }
}
