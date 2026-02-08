package com.agarg.securecollab.chatservice.bot;

import java.time.LocalDateTime;
import java.util.*;

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
