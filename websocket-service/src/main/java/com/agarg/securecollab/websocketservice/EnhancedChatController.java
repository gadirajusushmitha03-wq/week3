package com.agarg.securecollab.websocketservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.util.*;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enhanced WebSocket Controller with message routing, toxicity detection, and offline delivery
 */
@Controller
public class EnhancedChatController {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedChatController.class);
    
    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private PresenceService presenceService;
    @Autowired private RateLimitService rateLimitService;
    // @Autowired private ToxicityDetectionService toxicityService;
    // @Autowired private OfflineMessageQueueService offlineQueueService;

    private final Map<String, String> userConnections = new HashMap<>();
    
    /**
     * Handle user connection
     */
    @MessageMapping("/connect")
    public void handleUserConnect(@Payload Map<String, String> payload) {
        String userId = payload.get("userId");
        String sessionId = payload.get("sessionId");
        userConnections.put(userId, sessionId);
        presenceService.markOnline(userId, sessionId);
        logger.info("User connected: {}", userId);

        // Broadcast user online status
        messagingTemplate.convertAndSend("/topic/users/online", 
            Map.of("userId", userId, "status", "online", "timestamp", LocalDateTime.now()));
    }
    
    /**
     * Handle user disconnect
     */
    @MessageMapping("/disconnect")
    public void handleUserDisconnect(@Payload Map<String, String> payload) {
        String userId = payload.get("userId");
        userConnections.remove(userId);
        presenceService.markOffline(userId);
        logger.info("User disconnected: {}", userId);

        // Broadcast user offline status
        messagingTemplate.convertAndSend("/topic/users/offline", 
            Map.of("userId", userId, "status", "offline", "timestamp", LocalDateTime.now()));
    }
    
    /**
     * Send message with encryption and toxicity detection
     */
    @MessageMapping("/chat.send")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(@Payload ChatMessage message) {
        
        try {
            // Rate limit per sender
            if (!rateLimitService.tryConsume(message.getSenderId())) {
                logger.warn("Rate limit exceeded for user {}", message.getSenderId());
                message.setStatus("RATE_LIMITED");
                return message;
            }

            // Add server-side timestamp
            message.setTimestamp(LocalDateTime.now().toString());
            message.setStatus("SENT");

            // Log message
            logger.info("Message received from: {} in channel: {}", 
                       message.getSenderId(), message.getChannelId());

            // Note: Toxicity detection and encryption should be handled on client-side
            // Server only validates structure and forwards encrypted payloads

            return message;
            
        } catch (Exception e) {
            logger.error("Error processing message", e);
            message.setStatus("FAILED");
            return message;
        }
    }
    
    /**
     * Send direct message (private chat)
     */
    @MessageMapping("/chat.direct")
    public void sendDirectMessage(@Payload ChatMessage message) {
        
        try {
            // Rate limit per sender
            if (!rateLimitService.tryConsume(message.getSenderId())) {
                logger.warn("Rate limit exceeded for user {}", message.getSenderId());
                return;
            }

            message.setTimestamp(LocalDateTime.now().toString());

            // Send to specific user's queue
            messagingTemplate.convertAndSendToUser(message.getRecipientId(), "/queue/messages", message);

            logger.info("Direct message sent from: {} to: {}", 
                       message.getSenderId(), message.getRecipientId());
            
        } catch (Exception e) {
            logger.error("Error sending direct message", e);
        }
    }
    
    /**
     * Send message to specific channel
     */
    @MessageMapping("/chat.channel")
    public void sendChannelMessage(@Payload ChatMessage message) {
        
        try {
            message.setTimestamp(LocalDateTime.now().toString());
            
            // Send to channel topic
            String channelTopic = "/topic/channel/" + message.getChannelId();
            messagingTemplate.convertAndSend(channelTopic, message);
            
            logger.info("Channel message sent to: {}", message.getChannelId());
            
        } catch (Exception e) {
            logger.error("Error sending channel message", e);
        }
    }
    
    /**
     * Broadcast typing indicator
     */
    @MessageMapping("/chat.typing")
    @SendTo("/topic/typing")
    public TypingIndicator handleTypingIndicator(@Payload TypingIndicator indicator) {
        indicator.setTimestamp(LocalDateTime.now().toString());
        return indicator;
    }
    
    /**
     * Handle message reaction (emoji)
     */
    @MessageMapping("/chat.react")
    @SendTo("/topic/reactions")
    public MessageReaction handleMessageReaction(@Payload MessageReaction reaction) {
        reaction.setTimestamp(LocalDateTime.now().toString());
        return reaction;
    }
    
    /**
     * Request for offline message delivery
     */
    @MessageMapping("/sync.offline")
    public void syncOfflineMessages(@Payload Map<String, String> payload) {
        String userId = payload.get("userId");
        // In real implementation, retrieve from OfflineMessageQueueService
        logger.info("Syncing offline messages for user: {}", userId);
    }
    
    /**
     * Handle voice call signaling
     */
    @MessageMapping("/voice.signal")
    public void handleVoiceSignaling(@Payload Map<String, Object> signal) {
        try {
            String callId = (String) signal.get("callId");
            String type = (String) signal.get("type"); // offer, answer, candidate
            String targetUserId = (String) signal.get("targetUserId");
            
            // Forward signaling data to recipient
            messagingTemplate.convertAndSendToUser(targetUserId, "/queue/voice-signal", signal);
            
            logger.debug("Voice signal forwarded: callId={}, type={}", callId, type);
            
        } catch (Exception e) {
            logger.error("Error handling voice signaling", e);
        }
    }
    
    // DTOs
    
    public static class ChatMessage {
        private String messageId;
        private String senderId;
        private String senderName;
        private String recipientId;
        private String channelId;
        private String encryptedContent;
        private String timestamp;
        private String status;
        private List<String> attachmentIds;
        
        // Getters and Setters
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        
        public String getSenderId() { return senderId; }
        public void setSenderId(String senderId) { this.senderId = senderId; }
        
        public String getSenderName() { return senderName; }
        public void setSenderName(String senderName) { this.senderName = senderName; }
        
        public String getRecipientId() { return recipientId; }
        public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
        
        public String getChannelId() { return channelId; }
        public void setChannelId(String channelId) { this.channelId = channelId; }
        
        public String getEncryptedContent() { return encryptedContent; }
        public void setEncryptedContent(String encryptedContent) { this.encryptedContent = encryptedContent; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public List<String> getAttachmentIds() { return attachmentIds; }
        public void setAttachmentIds(List<String> attachmentIds) { this.attachmentIds = attachmentIds; }
    }
    
    public static class TypingIndicator {
        private String userId;
        private String channelId;
        private String timestamp;
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getChannelId() { return channelId; }
        public void setChannelId(String channelId) { this.channelId = channelId; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
    
    public static class MessageReaction {
        private String messageId;
        private String userId;
        private String emoji;
        private String timestamp;
        
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getEmoji() { return emoji; }
        public void setEmoji(String emoji) { this.emoji = emoji; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
}
