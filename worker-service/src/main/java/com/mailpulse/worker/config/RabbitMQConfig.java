package com.mailpulse.worker.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String NOTIFICATIONS_EXCHANGE = "notifications.direct";
    public static final String EMAIL_QUEUE = "notifications.email";
    public static final String EMAIL_ROUTING_KEY = "notifications.email";

    public static final String DLX_EXCHANGE = "notifications.dlx";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
