package com.agarg.securecollab.chatservice.bot;

import java.util.*;

/**
 * Bot workflow definition
 */
public class BotWorkflow {
    private String workflowId;
    private String workflowName;
    private List<BotAction> actions;
    private Map<String, Object> triggerConditions;
    
    public BotWorkflow(String workflowName, List<BotAction> actions) {
        this.workflowId = UUID.randomUUID().toString();
        this.workflowName = workflowName;
        this.actions = actions;
        this.triggerConditions = new HashMap<>();
    }
    
    public String getWorkflowId() { return workflowId; }
    public String getWorkflowName() { return workflowName; }
    public List<BotAction> getActions() { return actions; }
    public Map<String, Object> getTriggerConditions() { return triggerConditions; }
}
