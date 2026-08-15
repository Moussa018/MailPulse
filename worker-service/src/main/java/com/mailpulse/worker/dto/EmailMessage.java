package com.mailpulse.worker.dto;

import java.time.Instant;
import java.util.UUID;

public record EmailMessage(
        UUID id,
        String to,
        String subject,
        String body,
        Instant createdAt
) {
}
