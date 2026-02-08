package com.agarg.securecollab.chatservice.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message Event Listener
 * Handles events from message-driven channels
 */
@Service
public class MessageEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageEventListener.class);
    
    @RabbitListener(queues = MessageDrivenAutomationConfig.MESSAGE_CREATED_QUEUE)
    public void handleMessageCreated(String message) {
        logger.info("Received message created event: {}", message);
    }
    
    @RabbitListener(queues = MessageDrivenAutomationConfig.BOT_TRIGGER_QUEUE)
    public void handleBotTrigger(String botCommand) {
        logger.info("Received bot trigger: {}", botCommand);
    }
    
    @RabbitListener(queues = MessageDrivenAutomationConfig.TOXICITY_CHECK_QUEUE)
    public void handleToxicityCheck(String messageId) {
        logger.info("Received toxicity check request for message: {}", messageId);
    }
}
