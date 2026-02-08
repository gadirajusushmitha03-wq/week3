package com.agarg.securecollab.chatservice.bot;

/**
 * Bot action to execute
 */
public class BotAction {
    private String actionId;
    private String actionType;
    private Map<String, Object> actionParams;
    
    public BotAction(String actionType, Map<String, Object> actionParams) {
        this.actionId = UUID.randomUUID().toString();
        this.actionType = actionType;
        this.actionParams = actionParams;
    }
    
    public String getActionId() { return actionId; }
    public String getActionType() { return actionType; }
    public Map<String, Object> getActionParams() { return actionParams; }
}
