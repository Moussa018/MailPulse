package com.mailpulse.worker.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    private UUID id;

    private String recipient;

    private String subject;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private String errorMessage;

    private Instant createdAt;

    private Instant updatedAt;
}
