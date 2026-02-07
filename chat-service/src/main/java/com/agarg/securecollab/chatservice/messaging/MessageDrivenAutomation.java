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
    public Queue messageCreatedQueue() {
        return new Queue(MESSAGE_CREATED_QUEUE, true);
    }
    
    @Bean
    public Queue botTriggerQueue() {
        return new Queue(BOT_TRIGGER_QUEUE, true);
    }
    
    @Bean
    public Queue toxicityCheckQueue() {
        return new Queue(TOXICITY_CHECK_QUEUE, true);
    }
    
    @Bean
    public Queue offlineDeliveryQueue() {
        return new Queue(OFFLINE_DELIVERY_QUEUE, true);
    }
    
    @Bean
    public Queue webhookQueue() {
        return new Queue(WEBHOOK_QUEUE, true);
    }
    
    /**
     * Bind queues to exchanges
     */
    @Bean
    public Binding messageCreatedBinding(Queue messageCreatedQueue, TopicExchange chatEventsExchange) {
        return BindingBuilder.bind(messageCreatedQueue)
            .to(chatEventsExchange)
            .with(ROUTING_MESSAGE_CREATED + ".#");
    }
    
    @Bean
    public Binding botTriggerBinding(Queue botTriggerQueue, TopicExchange botCommandsExchange) {
        return BindingBuilder.bind(botTriggerQueue)
            .to(botCommandsExchange)
            .with(ROUTING_BOT_TRIGGER + ".#");
    }
    
    @Bean
    public Binding toxicityCheckBinding(Queue toxicityCheckQueue, TopicExchange chatEventsExchange) {
        return BindingBuilder.bind(toxicityCheckQueue)
            .to(chatEventsExchange)
            .with(ROUTING_TOXICITY + ".#");
    }
    
    @Bean
    public Binding offlineDeliveryBinding(Queue offlineDeliveryQueue, TopicExchange chatEventsExchange) {
        return BindingBuilder.bind(offlineDeliveryQueue)
            .to(chatEventsExchange)
            .with(ROUTING_OFFLINE + ".#");
    }
    
    @Bean
    public Binding webhookBinding(Queue webhookQueue, TopicExchange integrationExchange) {
        return BindingBuilder.bind(webhookQueue)
            .to(integrationExchange)
            .with("webhook.#");
    }
}

/**
 * Message event listener and processor
 */
@Service
public class MessageEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageEventListener.class);
    
    @Autowired private RabbitTemplate rabbitTemplate;
    
    /**
     * Listen for message created events
     */
    @RabbitListener(queues = MessageDrivenAutomationConfig.MESSAGE_CREATED_QUEUE)
    public void onMessageCreated(MessageCreatedEvent event) {
        logger.info("Message created event: {} in channel: {}", event.getMessageId(), event.getChannelId());
        
        // Trigger toxicity check
        rabbitTemplate.convertAndSend(
            MessageDrivenAutomationConfig.CHAT_EVENTS_EXCHANGE,
            MessageDrivenAutomationConfig.ROUTING_TOXICITY + ".new",
            event
        );
        
        // Check for offline users
        rabbitTemplate.convertAndSend(
            MessageDrivenAutomationConfig.CHAT_EVENTS_EXCHANGE,
            MessageDrivenAutomationConfig.ROUTING_OFFLINE + ".check",
            event
        );
    }
    
    /**
     * Listen for toxicity check results
     */
    @RabbitListener(queues = MessageDrivenAutomationConfig.TOXICITY_CHECK_QUEUE)
    public void onToxicityCheck(ToxicityCheckEvent event) {
        logger.info("Toxicity check: messageId={}, severity={}", event.getMessageId(), event.getSeverity());
        
        if ("HIGH".equals(event.getSeverity())) {
            // Block or flag the message
            logger.warn("High toxicity detected in message: {}", event.getMessageId());
            // Send alert to moderators
            // Publish moderation event
        }
    }
    
    /**
     * Listen for offline delivery checks
     */
    @RabbitListener(queues = MessageDrivenAutomationConfig.OFFLINE_DELIVERY_QUEUE)
    public void onOfflineDeliveryCheck(OfflineDeliveryEvent event) {
        logger.info("Checking offline delivery for message: {}", event.getMessageId());
        
        // If recipient is offline, queue the message
        for (String recipientId : event.getRecipientIds()) {
            if (event.isUserOffline(recipientId)) {
                logger.debug("User {} is offline, queueing message", recipientId);
                // Add to offline queue
            }
        }
    }
    
    /**
     * Listen for bot trigger events
     */
    @RabbitListener(queues = MessageDrivenAutomationConfig.BOT_TRIGGER_QUEUE)
    public void onBotTrigger(BotTriggerEvent event) {
        logger.info("Bot trigger event: {} with workflow: {}", 
                   event.getEventType(), event.getWorkflowId());
        
        // Execute bot workflow
        // Update Jira, GitHub, trigger CI/CD, send notifications, etc.
    }
    
    /**
     * Listen for webhook events
     */
    @RabbitListener(queues = MessageDrivenAutomationConfig.WEBHOOK_QUEUE)
    public void onWebhook(WebhookEvent event) {
        logger.info("Webhook event from: {} type: {}", event.getSource(), event.getEventType());
        
        // Process webhook and trigger actions in chat
        // E.g., Post GitHub PR notification to channel
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

/**
 * Event DTOs
 */
class MessageCreatedEvent {
    private String messageId;
    private String channelId;
    private String senderId;
    private long timestamp;
    
    public MessageCreatedEvent(String messageId, String channelId, String senderId) {
        this.messageId = messageId;
        this.channelId = channelId;
        this.senderId = senderId;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getMessageId() { return messageId; }
    public String getChannelId() { return channelId; }
    public String getSenderId() { return senderId; }
    public long getTimestamp() { return timestamp; }
    
    public boolean isUserOffline(String userId) {
        return false; // Implement offline check
    }
}

class ToxicityCheckEvent {
    private String messageId;
    private float score;
    private String severity;
    
    public ToxicityCheckEvent(String messageId, float score) {
        this.messageId = messageId;
        this.score = score;
        this.severity = score >= 0.8 ? "HIGH" : score >= 0.5 ? "MEDIUM" : "LOW";
    }
    
    public String getMessageId() { return messageId; }
    public float getScore() { return score; }
    public String getSeverity() { return severity; }
}

class OfflineDeliveryEvent {
    private String messageId;
    private List<String> recipientIds;
    
    public OfflineDeliveryEvent(String messageId, List<String> recipientIds) {
        this.messageId = messageId;
        this.recipientIds = recipientIds;
    }
    
    public String getMessageId() { return messageId; }
    public List<String> getRecipientIds() { return recipientIds; }
    
    public boolean isUserOffline(String userId) {
        return false; // Implement offline check
    }
}

class BotTriggerEvent {
    private String workflowId;
    private String eventType;
    private long timestamp;
    
    public BotTriggerEvent(String workflowId, String eventType) {
        this.workflowId = workflowId;
        this.eventType = eventType;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getWorkflowId() { return workflowId; }
    public String getEventType() { return eventType; }
    public long getTimestamp() { return timestamp; }
}

class WebhookEvent {
    private String source;
    private String eventType;
    private Map<String, Object> payload;
    
    public WebhookEvent(String source, String eventType, Map<String, Object> payload) {
        this.source = source;
        this.eventType = eventType;
        this.payload = payload;
    }
    
    public String getSource() { return source; }
    public String getEventType() { return eventType; }
    public Map<String, Object> getPayload() { return payload; }
}
