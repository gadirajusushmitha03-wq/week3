package com.agarg.securecollab.chatservice.bot;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Bot event that triggers bot workflows
 */
public class BotEvent {
    private String eventId;
    private BotEventType eventType;
    private String source;
    private String channelId;
    private Map<String, Object> payload;
    private LocalDateTime timestamp;

    public enum BotEventType {
        MESSAGE_CREATED,
        MESSAGE_EDITED,
        MESSAGE_DELETED,
        USER_JOINED,
        USER_LEFT,
        CHANNEL_CREATED,
        WORKFLOW_TRIGGERED,
        INTEGRATION_WEBHOOK,
        REMINDER_TRIGGERED,
        APPROVAL_REQUESTED
    }

    public BotEvent(BotEventType eventType, String source, String channelId, Map<String, Object> payload) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.source = source;
        this.channelId = channelId;
        this.payload = payload != null ? payload : new HashMap<>();
        this.timestamp = LocalDateTime.now();
    }

    public String getEventId() { return eventId; }
    public BotEventType getEventType() { return eventType; }
    public String getSource() { return source; }
    public String getChannelId() { return channelId; }
    public Map<String, Object> getPayload() { return payload; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
