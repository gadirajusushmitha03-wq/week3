package com.agarg.securecollab.chatservice.messaging;

import java.util.Map;

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