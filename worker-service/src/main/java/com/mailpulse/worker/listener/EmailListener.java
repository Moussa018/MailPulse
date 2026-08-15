package com.mailpulse.worker.listener;

import com.mailpulse.worker.config.RabbitMQConfig;
import com.mailpulse.worker.domain.NotificationStatus;
import com.mailpulse.worker.dto.EmailMessage;
import com.mailpulse.worker.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailListener {

    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void handle(EmailMessage message) {
        log.info("Processing notification {}", message.id());

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(message.to());
        mail.setSubject(message.subject());
        mail.setText(message.body());
        mailSender.send(mail);

        notificationRepository.findById(message.id()).ifPresent(notification -> {
            notification.setStatus(NotificationStatus.SENT);
            notification.setUpdatedAt(Instant.now());
            notificationRepository.save(notification);
        });
    }
}
