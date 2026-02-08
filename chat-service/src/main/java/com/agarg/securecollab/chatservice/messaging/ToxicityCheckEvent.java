package com.agarg.securecollab.chatservice.messaging;

class ToxicityCheckEvent {
    private String messageId;
    private float score;
    private String severity;

    public ToxicityCheckEvent(String messageId, float score) {
        this.messageId = messageId;
        this.score = score;
        this.severity = score >= 0.8 ? "HIGH" : score >= 0.5 ? "MEDIUM" : "LOW";
    }

    public String getMessageId() { return messageId; }
    public float getScore() { return score; }
    public String getSeverity() { return severity; }
}