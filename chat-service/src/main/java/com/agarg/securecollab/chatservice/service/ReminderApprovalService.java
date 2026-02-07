package com.agarg.securecollab.chatservice.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reminders and Approvals Service
 * Handles reminders and approval workflows in chat
 * Author: Anurag Garg
 */
@Service
public class ReminderApprovalService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReminderApprovalService.class);
    
    private final Map<String, Reminder> reminders = new ConcurrentHashMap<>();
    private final Map<String, ApprovalRequest> approvalRequests = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    
    public ReminderApprovalService() {
        // Start reminder checker thread
        scheduler.scheduleAtFixedRate(this::checkReminders, 1, 1, TimeUnit.MINUTES);
    }
    
    /**
     * Create a new reminder
     */
    public Reminder createReminder(String userId, String channelId, String title, 
                                   String description, LocalDateTime remindAt) {
        Reminder reminder = new Reminder(userId, channelId, title, description, remindAt);
        reminders.put(reminder.getId(), reminder);
        logger.info("Reminder created: {} for user: {}", reminder.getId(), userId);
        return reminder;
    }
    
    /**
     * Check and trigger reminders
     */
    private void checkReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<String> triggeredReminders = new ArrayList<>();
        
        for (Map.Entry<String, Reminder> entry : reminders.entrySet()) {
            Reminder reminder = entry.getValue();
            if (reminder.isActive() && 
                reminder.getRemindAt().isBefore(now) && 
                !reminder.isTriggered()) {
                
                reminder.setTriggered(true);
                reminder.setTriggeredAt(now);
                triggeredReminders.add(entry.getKey());
                
                logger.info("Reminder triggered: {} for user: {}", 
                           reminder.getId(), reminder.getUserId());
            }
        }
    }
    
    /**
     * Get reminders for a user
     */
    public List<Reminder> getUserReminders(String userId) {
        return reminders.values().stream()
            .filter(r -> r.getUserId().equals(userId))
            .toList();
    }
    
    /**
     * Get triggered reminders for a user
     */
    public List<Reminder> getTriggeredReminders(String userId) {
        return getUserReminders(userId).stream()
            .filter(Reminder::isTriggered)
            .toList();
    }
    
    /**
     * Cancel a reminder
     */
    public void cancelReminder(String reminderId) {
        Reminder reminder = reminders.get(reminderId);
        if (reminder != null) {
            reminder.setActive(false);
            logger.info("Reminder cancelled: {}", reminderId);
        }
    }
    
    /**
     * Create an approval request
     */
    public ApprovalRequest createApprovalRequest(String requesterId, String channelId,
                                                 String title, String description,
                                                 List<String> approverIds) {
        ApprovalRequest request = new ApprovalRequest(requesterId, channelId, 
                                                      title, description, approverIds);
        approvalRequests.put(request.getId(), request);
        logger.info("Approval request created: {} for requestor: {}", request.getId(), requesterId);
        return request;
    }
    
    /**
     * Approve a request
     */
    public void approveRequest(String requestId, String approverId, String comment) {
        ApprovalRequest request = approvalRequests.get(requestId);
        if (request != null) {
            request.addApproval(approverId, true, comment);
            
            // Check if all approvals are done
            if (request.isAllApprovalsDone()) {
                request.setStatus(ApprovalRequest.ApprovalStatus.APPROVED);
                logger.info("Approval request approved: {}", requestId);
            }
        }
    }
    
    /**
     * Reject a request
     */
    public void rejectRequest(String requestId, String rejectorId, String comment) {
        ApprovalRequest request = approvalRequests.get(requestId);
        if (request != null) {
            request.setStatus(ApprovalRequest.ApprovalStatus.REJECTED);
            request.addApproval(rejectorId, false, comment);
            logger.info("Approval request rejected: {}", requestId);
        }
    }
    
    /**
     * Get pending approval requests
     */
    public List<ApprovalRequest> getPendingApprovalRequests(String approverId) {
        return approvalRequests.values().stream()
            .filter(r -> r.getApproverIds().contains(approverId))
            .filter(r -> r.getStatus() == ApprovalRequest.ApprovalStatus.PENDING)
            .toList();
    }
    
    public static class Reminder {
        private String id;
        private String userId;
        private String channelId;
        private String title;
        private String description;
        private LocalDateTime remindAt;
        private LocalDateTime createdAt;
        private LocalDateTime triggeredAt;
        private boolean active;
        private boolean triggered;
        private ReminderType type;
        
        public enum ReminderType {
            ONE_TIME, DAILY, WEEKLY, MONTHLY
        }
        
        public Reminder(String userId, String channelId, String title, 
                       String description, LocalDateTime remindAt) {
            this.id = UUID.randomUUID().toString();
            this.userId = userId;
            this.channelId = channelId;
            this.title = title;
            this.description = description;
            this.remindAt = remindAt;
            this.createdAt = LocalDateTime.now();
            this.active = true;
            this.triggered = false;
            this.type = ReminderType.ONE_TIME;
        }
        
        public String getId() { return id; }
        public String getUserId() { return userId; }
        public String getChannelId() { return channelId; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public LocalDateTime getRemindAt() { return remindAt; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getTriggeredAt() { return triggeredAt; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public boolean isTriggered() { return triggered; }
        public void setTriggered(boolean triggered) { this.triggered = triggered; }
        public void setTriggeredAt(LocalDateTime triggeredAt) { this.triggeredAt = triggeredAt; }
        public ReminderType getType() { return type; }
        public void setType(ReminderType type) { this.type = type; }
    }
    
    public static class ApprovalRequest {
        private String id;
        private String requesterId;
        private String channelId;
        private String title;
        private String description;
        private List<String> approverIds;
        private Map<String, Approval> approvals;
        private ApprovalStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime resolvedAt;
        
        public enum ApprovalStatus {
            PENDING, APPROVED, REJECTED, EXPIRED
        }
        
        public static class Approval {
            public String approverId;
            public boolean approved;
            public String comment;
            public LocalDateTime approvedAt;
            
            public Approval(String approverId, boolean approved, String comment) {
                this.approverId = approverId;
                this.approved = approved;
                this.comment = comment;
                this.approvedAt = LocalDateTime.now();
            }
        }
        
        public ApprovalRequest(String requesterId, String channelId, String title,
                             String description, List<String> approverIds) {
            this.id = UUID.randomUUID().toString();
            this.requesterId = requesterId;
            this.channelId = channelId;
            this.title = title;
            this.description = description;
            this.approverIds = new ArrayList<>(approverIds);
            this.approvals = new HashMap<>();
            this.status = ApprovalStatus.PENDING;
            this.createdAt = LocalDateTime.now();
        }
        
        public void addApproval(String approverId, boolean approved, String comment) {
            approvals.put(approverId, new Approval(approverId, approved, comment));
        }
        
        public boolean isAllApprovalsDone() {
            return approvals.size() == approverIds.size();
        }
        
        public boolean isAllApproved() {
            return isAllApprovalsDone() && 
                   approvals.values().stream().allMatch(a -> a.approved);
        }
        
        public String getId() { return id; }
        public String getRequesterId() { return requesterId; }
        public String getChannelId() { return channelId; }
        public String getTitle() { return title; }
        public List<String> getApproverIds() { return approverIds; }
        public ApprovalStatus getStatus() { return status; }
        public void setStatus(ApprovalStatus status) { this.status = status; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getResolvedAt() { return resolvedAt; }
        public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
        public Map<String, Approval> getApprovals() { return approvals; }
    }
}
