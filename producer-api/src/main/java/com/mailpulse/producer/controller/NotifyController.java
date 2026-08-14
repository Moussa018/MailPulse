package com.mailpulse.producer.controller;

import com.mailpulse.producer.config.RabbitMQConfig;
import com.mailpulse.producer.dto.EmailMessage;
import com.mailpulse.producer.dto.NotifyRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notify")
@RequiredArgsConstructor
public class NotifyController {

    private final RabbitTemplate rabbitTemplate;

    @PostMapping
    public ResponseEntity<Map<String, Object>> notify(@Valid @RequestBody NotifyRequest request) {
        EmailMessage message = EmailMessage.from(request);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATIONS_EXCHANGE,
                RabbitMQConfig.EMAIL_ROUTING_KEY,
                message
        );

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of("id", message.id(), "status", "queued"));
    }
}
