package com.agarg.securecollab.chatservice.messaging;

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