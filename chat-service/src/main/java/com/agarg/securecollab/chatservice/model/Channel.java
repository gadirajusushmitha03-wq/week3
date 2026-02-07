package com.agarg.securecollab.chatservice.model;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Chat Channel model
 * Supports organized conversations with members and settings
 * Author: Anurag Garg
 */
public class Channel {
    private String id;
    private String name;
    private String description;
    private ChannelType type;
    private String ownerId;
    private Set<String> memberIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ChannelSettings settings;
    private List<String> botIds;
    
    public enum ChannelType {
        PUBLIC, PRIVATE, DIRECT
    }
    
    public Channel() {
        this.memberIds = new HashSet<>();
        this.botIds = new ArrayList<>();
        this.settings = new ChannelSettings();
    }
    
    public Channel(String name, String description, ChannelType type, String ownerId) {
        this();
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.type = type;
        this.ownerId = ownerId;
        this.memberIds.add(ownerId);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public static class ChannelSettings {
        private boolean allowExternalIntegrations = true;
        private boolean allowBots = true;
        private boolean allowFileSharing = true;
        private boolean allowVoiceMessages = true;
        private int messageRetentionDays = 90;
        private List<String> pinnedMessageIds = new ArrayList<>();
        
        public boolean isAllowExternalIntegrations() { return allowExternalIntegrations; }
        public void setAllowExternalIntegrations(boolean allow) { this.allowExternalIntegrations = allow; }
        
        public boolean isAllowBots() { return allowBots; }
        public void setAllowBots(boolean allow) { this.allowBots = allow; }
        
        public boolean isAllowFileSharing() { return allowFileSharing; }
        public void setAllowFileSharing(boolean allow) { this.allowFileSharing = allow; }
        
        public boolean isAllowVoiceMessages() { return allowVoiceMessages; }
        public void setAllowVoiceMessages(boolean allow) { this.allowVoiceMessages = allow; }
        
        public int getMessageRetentionDays() { return messageRetentionDays; }
        public void setMessageRetentionDays(int days) { this.messageRetentionDays = days; }
        
        public List<String> getPinnedMessageIds() { return pinnedMessageIds; }
        public void setPinnedMessageIds(List<String> ids) { this.pinnedMessageIds = ids; }
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public ChannelType getType() { return type; }
    public void setType(ChannelType type) { this.type = type; }
    
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    
    public Set<String> getMemberIds() { return memberIds; }
    public void setMemberIds(Set<String> memberIds) { this.memberIds = memberIds; }
    
    public void addMember(String memberId) { this.memberIds.add(memberId); }
    public void removeMember(String memberId) { this.memberIds.remove(memberId); }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public ChannelSettings getSettings() { return settings; }
    public void setSettings(ChannelSettings settings) { this.settings = settings; }
    
    public List<String> getBotIds() { return botIds; }
    public void setBotIds(List<String> botIds) { this.botIds = botIds; }
    
    public void addBot(String botId) { this.botIds.add(botId); }
    public void removeBot(String botId) { this.botIds.remove(botId); }
}
