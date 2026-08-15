package com.mailpulse.worker.recovery;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailpulse.worker.config.RabbitMQConfig;
import com.mailpulse.worker.domain.NotificationStatus;
import com.mailpulse.worker.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
public class NotificationStatusRecoverer extends RepublishMessageRecoverer {

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    public NotificationStatusRecoverer(AmqpTemplate rabbitTemplate,
                                        NotificationRepository notificationRepository,
                                        ObjectMapper objectMapper) {
        super(rabbitTemplate, RabbitMQConfig.DLX_EXCHANGE, RabbitMQConfig.EMAIL_ROUTING_KEY);
        this.notificationRepository = notificationRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void recover(Message message, Throwable cause) {
        markDeadLettered(message, cause);
        super.recover(message, cause);
    }

    private void markDeadLettered(Message message, Throwable cause) {
        try {
            JsonNode node = objectMapper.readTree(message.getBody());
            UUID id = UUID.fromString(node.get("id").asText());
            notificationRepository.findById(id).ifPresent(notification -> {
                notification.setStatus(NotificationStatus.DEAD_LETTERED);
                notification.setErrorMessage(cause.getMessage());
                notification.setUpdatedAt(Instant.now());
                notificationRepository.save(notification);
            });
        } catch (Exception e) {
            log.error("Could not update notification status before dead-lettering", e);
        }
    }
}
