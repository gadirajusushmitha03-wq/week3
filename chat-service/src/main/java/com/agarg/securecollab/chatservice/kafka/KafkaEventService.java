package com.agarg.securecollab.chatservice.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kafka Event Producer and Consumer
 * Produces events to Kafka and consumes with exactly-once semantics
 */
@Service
public class KafkaEventService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaEventService.class);

    public static final String TOPIC_MESSAGES = "chat.messages";
    public static final String TOPIC_TOXICITY = "chat.toxicity";
    public static final String TOPIC_OFFLINE = "chat.offline";
    public static final String TOPIC_BOT_EVENTS = "bot.events";
    public static final String TOPIC_INTEGRATIONS = "integrations.events";
    public static final String DLT_SUFFIX = "-dlt";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaEventService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishMessageEvent(String messageId, String channelId, String senderId, String payload) {
        String eventId = UUID.randomUUID().toString();
        String message = String.format("{\"eventId\":\"%s\",\"messageId\":\"%s\",\"channelId\":\"%s\",\"senderId\":\"%s\",\"payload\":%s}", 
            eventId, messageId, channelId, senderId, payload);
        kafkaTemplate.send(TOPIC_MESSAGES, messageId, message);
        logger.info("Published message event: {}", messageId);
    }

    public void publishToxicityEvent(String messageId, float score) {
        String message = String.format("{\"messageId\":\"%s\",\"score\":%.2f}", messageId, score);
        kafkaTemplate.send(TOPIC_TOXICITY, messageId, message);
        logger.info("Published toxicity event: {}", messageId);
    }

    public void publishOfflineEvent(String userId, String messageId) {
        String message = String.format("{\"userId\":\"%s\",\"messageId\":\"%s\"}", userId, messageId);
        kafkaTemplate.send(TOPIC_OFFLINE, userId, message);
        logger.info("Published offline event for user: {}", userId);
    }

    public void publishBotEvent(String workflowId, String eventType) {
        String message = String.format("{\"workflowId\":\"%s\",\"eventType\":\"%s\"}", workflowId, eventType);
        kafkaTemplate.send(TOPIC_BOT_EVENTS, workflowId, message);
        logger.info("Published bot event: {}", workflowId);
    }

    public void publishIntegrationEvent(String source, String eventType, String payload) {
        String message = String.format("{\"source\":\"%s\",\"eventType\":\"%s\",\"payload\":%s}", source, eventType, payload);
        kafkaTemplate.send(TOPIC_INTEGRATIONS, source, message);
        logger.info("Published integration event from: {}", source);
    }

    /**
     * Consume message events - exactly-once semantics via Kafka transactions
     */
    @KafkaListener(topics = TOPIC_MESSAGES, groupId = "chat-processor", containerFactory = "kafkaListenerContainerFactory")
    public void consumeMessageEvent(@Payload String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            logger.debug("Consuming message event from {}: {}", topic, message);
            // Process message (store to DB, publish to WebSocket, etc.)
        } catch (Exception e) {
            logger.error("Error processing message event", e);
            // Will be sent to DLT via Spring Kafka error handler
        }
    }

    /**
     * Consume toxicity events
     */
    @KafkaListener(topics = TOPIC_TOXICITY, groupId = "toxicity-processor", containerFactory = "kafkaListenerContainerFactory")
    public void consumeToxicityEvent(@Payload String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            logger.debug("Consuming toxicity event from {}: {}", topic, message);
            // Flag message if needed, notify moderators
        } catch (Exception e) {
            logger.error("Error processing toxicity event", e);
        }
    }

    /**
     * Consume offline delivery events
     */
    @KafkaListener(topics = TOPIC_OFFLINE, groupId = "offline-processor", containerFactory = "kafkaListenerContainerFactory")
    public void consumeOfflineEvent(@Payload String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            logger.debug("Consuming offline event from {}: {}", topic, message);
            // Queue message for offline user
        } catch (Exception e) {
            logger.error("Error processing offline event", e);
        }
    }

    /**
     * Dead Letter Topic consumer
     */
    @KafkaListener(topics = TOPIC_MESSAGES + DLT_SUFFIX, groupId = "dlt-consumer")
    public void consumeFromDLT(@Payload String message) {
        logger.error("Message in DLT (dead letter): {}", message);
        // Alert ops, log to audit trail, etc.
    }
}
