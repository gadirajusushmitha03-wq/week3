package com.agarg.securecollab.chatservice.bot;

import java.time.LocalDateTime;
import java.util.*;

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
