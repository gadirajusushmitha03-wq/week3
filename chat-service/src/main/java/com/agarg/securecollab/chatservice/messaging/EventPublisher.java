package com.agarg.securecollab.chatservice.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Event Publisher
 * Publishes events to message-driven channels
 */
@Service
public class EventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishEvent(String event) {
        logger.info("Publishing event: {}", event);
        // Add logic to publish event
    }

    public void publishMessageCreatedEvent(String messageId, String channelId, String senderId) {
        MessageCreatedEvent event = new MessageCreatedEvent(messageId, channelId, senderId);
        rabbitTemplate.convertAndSend(
            MessageDrivenAutomationConfig.CHAT_EVENTS_EXCHANGE,
            MessageDrivenAutomationConfig.ROUTING_MESSAGE_CREATED + ".new",
            event
        );
        logger.info("Published message created event: {}", messageId);
    }
}