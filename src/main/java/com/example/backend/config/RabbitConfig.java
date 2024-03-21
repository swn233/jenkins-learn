package com.example.backend.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean("emailQueue")
    public Queue emailQueue(){
        return QueueBuilder.durable("test_mail").build();
    }
}
