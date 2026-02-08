package com.agarg.securecollab.chatservice.bot;

import java.util.*;

/**
 * Bot entity
 */
public class Bot {
    private String botId;
    private String botName;
    private String botDescription;
    private List<BotWorkflow> workflows;
    private boolean enabled;
    
    public Bot(String botName, String botDescription) {
        this.botId = UUID.randomUUID().toString();
        this.botName = botName;
        this.botDescription = botDescription;
        this.workflows = new ArrayList<>();
        this.enabled = true;
    }
    
    public String getBotId() { return botId; }
    public String getBotName() { return botName; }
    public String getBotDescription() { return botDescription; }
    public List<BotWorkflow> getWorkflows() { return workflows; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
