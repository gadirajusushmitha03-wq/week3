package com.agarg.securecollab.chatservice.messaging;

import java.util.List;

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