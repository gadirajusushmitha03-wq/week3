package com.agarg.securecollab.botservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bot_workflows")
public class BotWorkflow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "trigger_type", nullable = false)
    private String triggerType; // MESSAGE, MENTION, COMMAND, SCHEDULE, WEBHOOK
    
    @Column(name = "trigger_pattern")
    private String triggerPattern; // regex or keyword
    
    @Lob
    @Column(name = "workflow_config")
    private String workflowConfig; // JSON with actions
    
    @Column(name = "enabled")
    private Boolean enabled = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    public BotWorkflow() {}
    
    public BotWorkflow(String name, String triggerType, String triggerPattern, String workflowConfig) {
        this.name = name;
        this.triggerType = triggerType;
        this.triggerPattern = triggerPattern;
        this.workflowConfig = workflowConfig;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getTriggerType() { return triggerType; }
    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }
    
    public String getTriggerPattern() { return triggerPattern; }
    public void setTriggerPattern(String triggerPattern) { this.triggerPattern = triggerPattern; }
    
    public String getWorkflowConfig() { return workflowConfig; }
    public void setWorkflowConfig(String workflowConfig) { this.workflowConfig = workflowConfig; }
    
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
