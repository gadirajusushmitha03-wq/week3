package com.agarg.securecollab.botservice.service;

import com.agarg.securecollab.botservice.model.BotWorkflow;
import com.agarg.securecollab.botservice.repository.BotWorkflowRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class WorkflowEngineService {
    
    private static final Logger logger = LoggerFactory.getLogger(WorkflowEngineService.class);
    
    @Autowired
    private BotWorkflowRepository workflowRepository;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    private JiraIntegrationService jiraService;
    
    @Autowired
    private GitHubIntegrationService githubService;
    
    private final ObjectMapper mapper = new ObjectMapper();
    
    /**
     * Process incoming message and trigger matching workflows
     */
    public void processMessageEvent(String messageId, String content, String channelId, String userId) {
        try {
            List<BotWorkflow> workflows = workflowRepository.findByTriggerTypeAndEnabledTrue("MESSAGE");
            
            for (BotWorkflow workflow : workflows) {
                if (matchesTrigger(content, workflow.getTriggerPattern())) {
                    executeWorkflow(workflow, messageId, content, channelId, userId);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing message event", e);
        }
    }
    
    /**
     * Process mention events
     */
    public void processMentionEvent(String messageId, String content, String channelId, String userId, String botName) {
        try {
            List<BotWorkflow> workflows = workflowRepository.findByTriggerTypeAndEnabledTrue("MENTION");
            
            for (BotWorkflow workflow : workflows) {
                if (content.contains("@" + botName)) {
                    executeWorkflow(workflow, messageId, content, channelId, userId);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing mention event", e);
        }
    }
    
    /**
     * Execute workflow actions
     */
    private void executeWorkflow(BotWorkflow workflow, String messageId, String content, String channelId, String userId) {
        try {
            JsonNode config = mapper.readTree(workflow.getWorkflowConfig());
            JsonNode actions = config.get("actions");
            
            if (actions != null && actions.isArray()) {
                for (JsonNode action : actions) {
                    String actionType = action.get("type").asText();
                    
                    switch (actionType) {
                        case "JIRA_CREATE_ISSUE":
                            handleJiraIssueCreation(action, content, userId);
                            break;
                        case "GITHUB_CREATE_ISSUE":
                            handleGitHubIssueCreation(action, content, userId);
                            break;
                        case "SEND_REMINDER":
                            handleReminder(action, userId, channelId);
                            break;
                        case "REQUEST_APPROVAL":
                            handleApprovalRequest(action, userId, channelId, messageId);
                            break;
                        case "SEND_NOTIFICATION":
                            handleNotification(action, userId, channelId);
                            break;
                        default:
                            logger.warn("Unknown action type: {}", actionType);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error executing workflow: {}", workflow.getName(), e);
        }
    }
    
    private void handleJiraIssueCreation(JsonNode action, String content, String userId) {
        try {
            String projectKey = action.get("projectKey").asText();
            String issueType = action.get("issueType").asText("Task");
            
            logger.info("Creating Jira issue in project {} with type {}", projectKey, issueType);
            // Implementation delegates to JiraIntegrationService
            // jiraService.createIssue(projectKey, issueType, content, userId);
        } catch (Exception e) {
            logger.error("Error creating Jira issue", e);
        }
    }
    
    private void handleGitHubIssueCreation(JsonNode action, String content, String userId) {
        try {
            String repo = action.get("repository").asText();
            logger.info("Creating GitHub issue in repo {}", repo);
            // Implementation delegates to GitHubIntegrationService
            // githubService.createIssue(repo, content, userId);
        } catch (Exception e) {
            logger.error("Error creating GitHub issue", e);
        }
    }
    
    private void handleReminder(JsonNode action, String userId, String channelId) {
        try {
            String reminderText = action.get("text").asText();
            long delaySeconds = action.get("delaySeconds").asLong(3600);
            
            kafkaTemplate.send("reminders", userId, 
                String.format("{\"userId\":\"%s\",\"channelId\":\"%s\",\"text\":\"%s\",\"delaySeconds\":%d}",
                    userId, channelId, reminderText, delaySeconds));
            
            logger.info("Reminder scheduled for user {}", userId);
        } catch (Exception e) {
            logger.error("Error handling reminder", e);
        }
    }
    
    private void handleApprovalRequest(JsonNode action, String userId, String channelId, String messageId) {
        try {
            String approvers = action.get("approvers").asText();
            String approvalMessage = action.get("message").asText();
            
            kafkaTemplate.send("approvals", messageId,
                String.format("{\"messageId\":\"%s\",\"userId\":\"%s\",\"channelId\":\"%s\",\"approvers\":\"%s\",\"message\":\"%s\"}",
                    messageId, userId, channelId, approvers, approvalMessage));
            
            logger.info("Approval request sent for message {}", messageId);
        } catch (Exception e) {
            logger.error("Error handling approval request", e);
        }
    }
    
    private void handleNotification(JsonNode action, String userId, String channelId) {
        try {
            String notificationMessage = action.get("message").asText();
            String notificationType = action.get("type").asText("INFO");
            
            kafkaTemplate.send("notifications", userId,
                String.format("{\"userId\":\"%s\",\"channelId\":\"%s\",\"message\":\"%s\",\"type\":\"%s\"}",
                    userId, channelId, notificationMessage, notificationType));
            
            logger.info("Notification sent to user {}", userId);
        } catch (Exception e) {
            logger.error("Error handling notification", e);
        }
    }
    
    private boolean matchesTrigger(String content, String pattern) {
        try {
            return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(content).find();
        } catch (Exception e) {
            return content.toLowerCase().contains(pattern.toLowerCase());
        }
    }
    
    /**
     * Create or update a workflow
     */
    public BotWorkflow saveWorkflow(BotWorkflow workflow) {
        workflow.setUpdatedAt(java.time.LocalDateTime.now());
        return workflowRepository.save(workflow);
    }
    
    /**
     * Get all enabled workflows
     */
    public List<BotWorkflow> getAllEnabledWorkflows() {
        return workflowRepository.findByEnabledTrue();
    }
}
