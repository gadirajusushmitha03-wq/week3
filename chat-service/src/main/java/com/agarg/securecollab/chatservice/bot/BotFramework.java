package com.agarg.securecollab.chatservice.bot;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Bot framework for event-driven automation and integrations
 * Author: Anurag Garg
 */

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

/**
 * Bot action that can be executed in response to events
 */
public class BotAction {
    private String actionId;
    private BotActionType actionType;
    private String botId;
    private Map<String, Object> config;
    private LocalDateTime createdAt;
    
    public enum BotActionType {
        SEND_MESSAGE,
        CREATE_TICKET,
        NOTIFY_USER,
        TRIGGER_CI_CD,
        SYNC_JIRA,
        SYNC_GITHUB,
        UPDATE_STATUS,
        REQUEST_APPROVAL,
        SET_REMINDER,
        EXECUTE_WORKFLOW
    }
    
    public BotAction(BotActionType actionType, String botId, Map<String, Object> config) {
        this.actionId = UUID.randomUUID().toString();
        this.actionType = actionType;
        this.botId = botId;
        this.config = config != null ? config : new HashMap<>();
        this.createdAt = LocalDateTime.now();
    }
    
    public String getActionId() { return actionId; }
    public BotActionType getActionType() { return actionType; }
    public String getBotId() { return botId; }
    public Map<String, Object> getConfig() { return config; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

/**
 * Bot workflow definition
 */
public class BotWorkflow {
    private String workflowId;
    private String name;
    private String description;
    private String botId;
    private List<WorkflowTrigger> triggers;
    private List<WorkflowAction> actions;
    private WorkflowStatus status;
    private LocalDateTime createdAt;
    
    public enum WorkflowStatus {
        ACTIVE, INACTIVE, ERROR
    }
    
    public static class WorkflowTrigger {
        private BotEvent.BotEventType eventType;
        private Map<String, Object> conditions;
        
        public WorkflowTrigger(BotEvent.BotEventType eventType, Map<String, Object> conditions) {
            this.eventType = eventType;
            this.conditions = conditions != null ? conditions : new HashMap<>();
        }
        
        public BotEvent.BotEventType getEventType() { return eventType; }
        public Map<String, Object> getConditions() { return conditions; }
    }
    
    public static class WorkflowAction {
        private BotAction.BotActionType actionType;
        private Map<String, Object> config;
        private int order;
        
        public WorkflowAction(BotAction.BotActionType actionType, Map<String, Object> config, int order) {
            this.actionType = actionType;
            this.config = config != null ? config : new HashMap<>();
            this.order = order;
        }
        
        public BotAction.BotActionType getActionType() { return actionType; }
        public Map<String, Object> getConfig() { return config; }
        public int getOrder() { return order; }
    }
    
    public BotWorkflow(String name, String description, String botId) {
        this.workflowId = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.botId = botId;
        this.triggers = new ArrayList<>();
        this.actions = new ArrayList<>();
        this.status = WorkflowStatus.INACTIVE;
        this.createdAt = LocalDateTime.now();
    }
    
    public String getWorkflowId() { return workflowId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getBotId() { return botId; }
    public List<WorkflowTrigger> getTriggers() { return triggers; }
    public List<WorkflowAction> getActions() { return actions; }
    public WorkflowStatus getStatus() { return status; }
    public void setStatus(WorkflowStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    public void addTrigger(WorkflowTrigger trigger) { this.triggers.add(trigger); }
    public void addAction(WorkflowAction action) { this.actions.add(action); }
}

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
