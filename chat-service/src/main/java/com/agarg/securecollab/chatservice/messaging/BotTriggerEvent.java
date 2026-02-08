package com.agarg.securecollab.chatservice.messaging;

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