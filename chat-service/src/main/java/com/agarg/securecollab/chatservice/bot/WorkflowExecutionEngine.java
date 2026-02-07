package com.agarg.securecollab.chatservice.bot;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bot Workflow Execution Engine
 * Executes workflows with state machine, retries, and compensation
 */
@Service
public class WorkflowExecutionEngine {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowExecutionEngine.class);

    private final Map<String, WorkflowExecution> executions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(8);

    /**
     * Start a workflow execution
     */
    public WorkflowExecution startExecution(BotWorkflow workflow, Map<String, Object> context) {
        String executionId = UUID.randomUUID().toString();
        WorkflowExecution exec = new WorkflowExecution(executionId, workflow, context);
        executions.put(executionId, exec);

        logger.info("Started workflow execution: {}", executionId);

        // Schedule async execution
        executor.execute(() -> executeWorkflow(exec));

        return exec;
    }

    private void executeWorkflow(WorkflowExecution execution) {
        try {
            execution.setState(WorkflowState.RUNNING);
            execution.setStartedAt(LocalDateTime.now());

            for (BotWorkflow.WorkflowAction action : execution.getWorkflow().getActions()) {
                if (execution.getState() == WorkflowState.CANCELLED) {
                    break;
                }

                try {
                    executeAction(execution, action);
                    execution.addCompletedAction(action.getActionType());
                } catch (Exception e) {
                    logger.error("Action failed: {}", action.getActionType(), e);
                    execution.addFailedAction(action.getActionType(), e.getMessage());

                    // Retry logic
                    if (action.getConfig().getOrDefault("retryCount", 0) instanceof Integer retries && retries > 0) {
                        int delay = (int) action.getConfig().getOrDefault("retryDelayMs", 1000);
                        Thread.sleep(delay);
                        executeAction(execution, action);
                    } else {
                        execution.setState(WorkflowState.FAILED);
                        return;
                    }
                }
            }

            execution.setState(WorkflowState.COMPLETED);
            execution.setCompletedAt(LocalDateTime.now());
            logger.info("Workflow completed: {}", execution.getId());

        } catch (Exception e) {
            logger.error("Workflow execution failed: {}", execution.getId(), e);
            execution.setState(WorkflowState.FAILED);
            execution.setError(e.getMessage());
        }
    }

    private void executeAction(WorkflowExecution execution, BotWorkflow.WorkflowAction action) throws Exception {
        BotAction.BotActionType type = action.getActionType();

        switch (type) {
            case SEND_MESSAGE:
                handleSendMessage(execution, action);
                break;
            case CREATE_TICKET:
                handleCreateTicket(execution, action);
                break;
            case NOTIFY_USER:
                handleNotifyUser(execution, action);
                break;
            case TRIGGER_CI_CD:
                handleTriggerCICD(execution, action);
                break;
            case UPDATE_STATUS:
                handleUpdateStatus(execution, action);
                break;
            default:
                logger.warn("Unknown action type: {}", type);
        }
    }

    private void handleSendMessage(WorkflowExecution exec, BotWorkflow.WorkflowAction action) {
        String message = (String) action.getConfig().get("message");
        String channelId = (String) action.getConfig().get("channelId");
        logger.info("Bot action: sending message to channel {}: {}", channelId, message);
    }

    private void handleCreateTicket(WorkflowExecution exec, BotWorkflow.WorkflowAction action) {
        String title = (String) action.getConfig().get("title");
        String system = (String) action.getConfig().getOrDefault("system", "JIRA");
        logger.info("Bot action: creating {} ticket: {}", system, title);
    }

    private void handleNotifyUser(WorkflowExecution exec, BotWorkflow.WorkflowAction action) {
        String userId = (String) action.getConfig().get("userId");
        String message = (String) action.getConfig().get("message");
        logger.info("Bot action: notifying user {}: {}", userId, message);
    }

    private void handleTriggerCICD(WorkflowExecution exec, BotWorkflow.WorkflowAction action) {
        String pipeline = (String) action.getConfig().get("pipeline");
        String environment = (String) action.getConfig().getOrDefault("environment", "staging");
        logger.info("Bot action: triggering CI/CD pipeline {} on {}", pipeline, environment);
    }

    private void handleUpdateStatus(WorkflowExecution exec, BotWorkflow.WorkflowAction action) {
        String status = (String) action.getConfig().get("status");
        logger.info("Bot action: updating status to {}", status);
    }

    public WorkflowExecution getExecution(String executionId) {
        return executions.get(executionId);
    }

    public void cancelExecution(String executionId) {
        WorkflowExecution exec = executions.get(executionId);
        if (exec != null) {
            exec.setState(WorkflowState.CANCELLED);
            logger.info("Cancelled workflow execution: {}", executionId);
        }
    }

    public enum WorkflowState {
        PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
    }

    public static class WorkflowExecution {
        private String id;
        private BotWorkflow workflow;
        private Map<String, Object> context;
        private WorkflowState state;
        private LocalDateTime createdAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private List<String> completedActions;
        private Map<String, String> failedActions;
        private String error;

        public WorkflowExecution(String id, BotWorkflow workflow, Map<String, Object> context) {
            this.id = id;
            this.workflow = workflow;
            this.context = context;
            this.state = WorkflowState.PENDING;
            this.createdAt = LocalDateTime.now();
            this.completedActions = new ArrayList<>();
            this.failedActions = new HashMap<>();
        }

        public void addCompletedAction(BotAction.BotActionType action) {
            completedActions.add(action.toString());
        }

        public void addFailedAction(BotAction.BotActionType action, String error) {
            failedActions.put(action.toString(), error);
        }

        // Getters
        public String getId() { return id; }
        public BotWorkflow getWorkflow() { return workflow; }
        public Map<String, Object> getContext() { return context; }
        public WorkflowState getState() { return state; }
        public void setState(WorkflowState state) { this.state = state; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getStartedAt() { return startedAt; }
        public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
        public LocalDateTime getCompletedAt() { return completedAt; }
        public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
        public List<String> getCompletedActions() { return completedActions; }
        public Map<String, String> getFailedActions() { return failedActions; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
