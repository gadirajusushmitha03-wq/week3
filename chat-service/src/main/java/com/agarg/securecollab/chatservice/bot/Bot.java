package com.agarg.securecollab.chatservice.bot;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Bot definition
 */
public class Bot {
    private String botId;
    private String name;
    private String description;
    private BotType botType;
    private String apiKey;
    private Map<String, Object> config;
    private List<String> workflows;
    private BotStatus status;
    private LocalDateTime createdAt;

    public enum BotType {
        WORKFLOW_AUTOMATION,
        INTEGRATION_CONNECTOR,
        CI_CD_TRIGGER,
        NOTIFICATION_BOT,
        APPROVAL_BOT,
        REMINDER_BOT
    }

    public enum BotStatus {
        ACTIVE, INACTIVE, ERROR, SUSPENDED
    }

    public Bot(String name, String description, BotType botType) {
        this.botId = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.botType = botType;
        this.apiKey = UUID.randomUUID().toString();
        this.config = new HashMap<>();
        this.workflows = new ArrayList<>();
        this.status = BotStatus.INACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    public String getBotId() { return botId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BotType getBotType() { return botType; }
    public String getApiKey() { return apiKey; }
    public Map<String, Object> getConfig() { return config; }
    public List<String> getWorkflows() { return workflows; }
    public BotStatus getStatus() { return status; }
    public void setStatus(BotStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void addWorkflow(String workflowId) { this.workflows.add(workflowId); }
}
