package com.mailpulse.producer.controller;

import com.mailpulse.producer.config.RabbitMQConfig;
import com.mailpulse.producer.domain.Notification;
import com.mailpulse.producer.domain.NotificationStatus;
import com.mailpulse.producer.dto.EmailMessage;
import com.mailpulse.producer.dto.NotifyRequest;
import com.mailpulse.producer.repository.NotificationRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notify")
@RequiredArgsConstructor
public class NotifyController {

    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:";
    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);

    private final RabbitTemplate rabbitTemplate;
    private final NotificationRepository notificationRepository;
    private final StringRedisTemplate redisTemplate;

    @PostMapping
    public ResponseEntity<Map<String, Object>> notify(
            @Valid @RequestBody NotifyRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        UUID id = UUID.randomUUID();

        if (idempotencyKey != null) {
            String redisKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
            boolean firstSeen = Boolean.TRUE.equals(
                    redisTemplate.opsForValue().setIfAbsent(redisKey, id.toString(), IDEMPOTENCY_TTL));

            if (!firstSeen) {
                String existingId = redisTemplate.opsForValue().get(redisKey);
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(Map.of("id", existingId, "status", "queued", "duplicate", true));
            }
        }

        Instant now = Instant.now();
        Notification notification = new Notification(
                id, request.to(), request.subject(), NotificationStatus.QUEUED, null, now, now);
        notificationRepository.save(notification);

        EmailMessage message = new EmailMessage(id, request.to(), request.subject(), request.body(), now);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATIONS_EXCHANGE,
                RabbitMQConfig.EMAIL_ROUTING_KEY,
                message
        );

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of("id", id, "status", "queued"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getStatus(@PathVariable UUID id) {
        return notificationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
