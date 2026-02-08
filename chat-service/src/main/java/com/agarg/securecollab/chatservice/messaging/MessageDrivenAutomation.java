package com.agarg.securecollab.chatservice.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message-Driven Automation using RabbitMQ
 * Processes events and triggers automated workflows
 * Author: Anurag Garg
 */
@Configuration
public class MessageDrivenAutomationConfig {
    
    // Exchanges
    public static final String CHAT_EVENTS_EXCHANGE = "chat.events";
    public static final String BOT_COMMANDS_EXCHANGE = "bot.commands";
    public static final String INTEGRATION_EXCHANGE = "integrations";
    
    // Queues
    public static final String MESSAGE_CREATED_QUEUE = "queue.message.created";
    public static final String BOT_TRIGGER_QUEUE = "queue.bot.trigger";
    public static final String TOXICITY_CHECK_QUEUE = "queue.toxicity.check";
    public static final String OFFLINE_DELIVERY_QUEUE = "queue.offline.delivery";
    public static final String WEBHOOK_QUEUE = "queue.webhooks";
    
    // Routing keys
    public static final String ROUTING_MESSAGE_CREATED = "message.created";
    public static final String ROUTING_BOT_TRIGGER = "bot.trigger";
    public static final String ROUTING_TOXICITY = "toxicity.check";
    public static final String ROUTING_OFFLINE = "offline.delivery";
    
    /**
     * Declare exchanges
     */
    @Bean
    public TopicExchange chatEventsExchange() {
        return new TopicExchange(CHAT_EVENTS_EXCHANGE, true, false);
    }
    
    @Bean
    public TopicExchange botCommandsExchange() {
        return new TopicExchange(BOT_COMMANDS_EXCHANGE, true, false);
    }
    
    @Bean
    public TopicExchange integrationExchange() {
        return new TopicExchange(INTEGRATION_EXCHANGE, true, false);
    }
    
    /**
     * Declare queues
     */
    @Bean
    public org.springframework.amqp.core.Queue messageCreatedQueue() {
        return new org.springframework.amqp.core.Queue(MESSAGE_CREATED_QUEUE, true);
    }
    
    @Bean
    public org.springframework.amqp.core.Queue botTriggerQueue() {
        return new org.springframework.amqp.core.Queue(BOT_TRIGGER_QUEUE, true);
    }
    
    @Bean
    public org.springframework.amqp.core.Queue toxicityCheckQueue() {
        return new org.springframework.amqp.core.Queue(TOXICITY_CHECK_QUEUE, true);
    }
    
    @Bean
    public org.springframework.amqp.core.Queue offlineDeliveryQueue() {
        return new org.springframework.amqp.core.Queue(OFFLINE_DELIVERY_QUEUE, true);
    }
    
    @Bean
    public org.springframework.amqp.core.Queue webhookQueue() {
        return new org.springframework.amqp.core.Queue(WEBHOOK_QUEUE, true);
    }
    
    /**
     * Bind queues to exchanges
     */
    @Bean
    public Binding messageCreatedBinding(org.springframework.amqp.core.Queue messageCreatedQueue, TopicExchange chatEventsExchange) {
        return BindingBuilder.bind(messageCreatedQueue)
            .to(chatEventsExchange)
            .with(ROUTING_MESSAGE_CREATED + ".#");
    }
    
    @Bean
    public Binding botTriggerBinding(org.springframework.amqp.core.Queue botTriggerQueue, TopicExchange botCommandsExchange) {
        return BindingBuilder.bind(botTriggerQueue)
            .to(botCommandsExchange)
            .with(ROUTING_BOT_TRIGGER + ".#");
    }
    
    @Bean
    public Binding toxicityCheckBinding(org.springframework.amqp.core.Queue toxicityCheckQueue, TopicExchange chatEventsExchange) {
        return BindingBuilder.bind(toxicityCheckQueue)
            .to(chatEventsExchange)
            .with(ROUTING_TOXICITY + ".#");
    }
    
    @Bean
    public Binding offlineDeliveryBinding(org.springframework.amqp.core.Queue offlineDeliveryQueue, TopicExchange chatEventsExchange) {
        return BindingBuilder.bind(offlineDeliveryQueue)
            .to(chatEventsExchange)
            .with(ROUTING_OFFLINE + ".#");
    }
    
    @Bean
    public Binding webhookBinding(org.springframework.amqp.core.Queue webhookQueue, TopicExchange integrationExchange) {
        return BindingBuilder.bind(webhookQueue)
            .to(integrationExchange)
            .with("webhook.#");
    }
}

/**
 * Event publishing service
 */
@Service
public class EventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);
    
    @Autowired private RabbitTemplate rabbitTemplate;
    
    public void publishMessageCreatedEvent(String messageId, String channelId, String senderId) {
        MessageCreatedEvent event = new MessageCreatedEvent(messageId, channelId, senderId);
        rabbitTemplate.convertAndSend(
            MessageDrivenAutomationConfig.CHAT_EVENTS_EXCHANGE,
            MessageDrivenAutomationConfig.ROUTING_MESSAGE_CREATED + ".new",
            event
        );
        logger.info("Published message created event: {}", messageId);
    }
    
    public void publishToxicityCheckEvent(String messageId, float score) {
        ToxicityCheckEvent event = new ToxicityCheckEvent(messageId, score);
        rabbitTemplate.convertAndSend(
            MessageDrivenAutomationConfig.CHAT_EVENTS_EXCHANGE,
            MessageDrivenAutomationConfig.ROUTING_TOXICITY + ".check",
            event
        );
    }
    
    public void publishBotTriggerEvent(String workflowId, String eventType) {
        BotTriggerEvent event = new BotTriggerEvent(workflowId, eventType);
        rabbitTemplate.convertAndSend(
            MessageDrivenAutomationConfig.BOT_COMMANDS_EXCHANGE,
            MessageDrivenAutomationConfig.ROUTING_BOT_TRIGGER + ".execute",
            event
        );
        logger.info("Published bot trigger event: {}", workflowId);
    }
}
