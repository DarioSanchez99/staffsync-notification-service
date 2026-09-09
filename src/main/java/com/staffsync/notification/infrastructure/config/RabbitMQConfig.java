package com.staffsync.notification.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "staffsync.notifications";
    public static final String QUEUE_NAME = "staffsync.vacation.notifications";
    public static final String ROUTING_KEY = "vacation.#";

    @Bean
    public TopicExchange staffsyncNotificationsExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue vacationNotificationsQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding vacationNotificationsBinding(Queue vacationNotificationsQueue,
                                                 TopicExchange staffsyncNotificationsExchange) {
        return BindingBuilder
                .bind(vacationNotificationsQueue)
                .to(staffsyncNotificationsExchange)
                .with(ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
