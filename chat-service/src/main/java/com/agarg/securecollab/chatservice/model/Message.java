package com.agarg.securecollab.chatservice.model;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Message domain model
 * Author: Anurag Garg
 */
public class Message {
    private String id;
    private String channelId;
    private String senderId;
    private String senderName;
    private String encryptedContent;
    private LocalDateTime timestamp;
    private MessageStatus status;
    private Map<String, Object> metadata;
    private List<String> attachmentIds;
    private boolean edited;
    private LocalDateTime editedAt;
    private MessageReaction reactions;
    
    public enum MessageStatus {
        SENT, DELIVERED, FAILED, PENDING_OFFLINE
    }
    
    public Message() {
        this.metadata = new HashMap<>();
        this.attachmentIds = new ArrayList<>();
        this.reactions = new MessageReaction();
        this.status = MessageStatus.PENDING_OFFLINE;
        this.edited = false;
    }
    
    public Message(String channelId, String senderId, String encryptedContent) {
        this();
        this.id = UUID.randomUUID().toString();
        this.channelId = channelId;
        this.senderId = senderId;
        this.encryptedContent = encryptedContent;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }
    
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    
    public String getEncryptedContent() { return encryptedContent; }
    public void setEncryptedContent(String encryptedContent) { this.encryptedContent = encryptedContent; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public List<String> getAttachmentIds() { return attachmentIds; }
    public void setAttachmentIds(List<String> attachmentIds) { this.attachmentIds = attachmentIds; }
    
    public boolean isEdited() { return edited; }
    public void setEdited(boolean edited) { this.edited = edited; }
    
    public LocalDateTime getEditedAt() { return editedAt; }
    public void setEditedAt(LocalDateTime editedAt) { this.editedAt = editedAt; }
    
    public MessageReaction getReactions() { return reactions; }
    public void setReactions(MessageReaction reactions) { this.reactions = reactions; }
    
    public static class MessageReaction {
        private Map<String, Set<String>> reactions = new HashMap<>();
        
        public void addReaction(String emoji, String userId) {
            reactions.computeIfAbsent(emoji, k -> new HashSet<>()).add(userId);
        }
        
        public void removeReaction(String emoji, String userId) {
            Set<String> users = reactions.get(emoji);
            if (users != null) {
                users.remove(userId);
                if (users.isEmpty()) {
                    reactions.remove(emoji);
                }
            }
        }
        
        public Map<String, Integer> getReactionCounts() {
            Map<String, Integer> counts = new HashMap<>();
            reactions.forEach((emoji, users) -> counts.put(emoji, users.size()));
            return counts;
        }
    }
}
