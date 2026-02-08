package com.agarg.securecollab.chatservice.controller;

import com.agarg.securecollab.chatservice.model.*;
import com.agarg.securecollab.chatservice.service.*;
import com.agarg.securecollab.chatservice.messaging.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST API Controller for Chat Operations
 * Author: Anurag Garg
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatApiController {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatApiController.class);
    
    @Autowired private ToxicityDetectionService toxicityService;
    @Autowired private EncryptionService encryptionService;
    @Autowired private OfflineMessageQueueService offlineQueueService;
    @Autowired private ReminderApprovalService reminderApprovalService;
    @Autowired private FileSharingService fileSharingService;
    @Autowired private VoiceCallService voiceCallService;
    @Autowired private EventPublisher eventPublisher;
    @Autowired private com.agarg.securecollab.chatservice.repository.MessageRepository messageRepository;
    
    /**
     * Check message toxicity
     */
    @PostMapping("/analyze-toxicity")
    public ResponseEntity<?> analyzeToxicity(@RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            ToxicityDetectionService.ToxicityAnalysis analysis = toxicityService.analyzeToxicity(message);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            logger.error("Error analyzing toxicity", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error analyzing toxicity");
        }
    }
    
    /**
     * Send encrypted message
     */
    @PostMapping("/send-message")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, String> request) {
        try {
            String plaintext = request.get("message");
            String keyId = request.get("keyId");
            
            EncryptionService.EncryptedPayload encrypted = encryptionService.encrypt(plaintext, keyId);
            // Persist message (server stores only encrypted payload)
            String messageId = java.util.UUID.randomUUID().toString();
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            com.agarg.securecollab.chatservice.entity.MessageEntity me = new com.agarg.securecollab.chatservice.entity.MessageEntity(
                messageId,
                request.getOrDefault("channelId", ""),
                request.getOrDefault("senderId", ""),
                encrypted.getCiphertext(),
                "SENT",
                now
            );
            messageRepository.save(me);

            // Publish event for downstream processing (toxicity, offline delivery, bots)
            eventPublisher.publishMessageCreatedEvent(messageId, me.getChannelId(), me.getSenderId());

            Map<String, Object> resp = new HashMap<>();
            resp.put("messageId", messageId);
            resp.put("encrypted", encrypted);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            logger.error("Error sending message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending message");
        }
    }
    
    /**
     * Get pending messages for offline user
     */
    @GetMapping("/pending-messages/{userId}")
    public ResponseEntity<?> getPendingMessages(@PathVariable String userId) {
        try {
            List<Message> messages = offlineQueueService.markUserOnlineAndRetrieveMessages(userId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            logger.error("Error retrieving pending messages", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving messages");
        }
    }
    
    /**
     * Get offline queue statistics
     */
    @GetMapping("/queue-stats")
    public ResponseEntity<?> getQueueStats() {
        try {
            Map<String, Object> stats = offlineQueueService.getQueueStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error retrieving queue stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving stats");
        }
    }
    
    /**
     * Create a reminder
     */
    @PostMapping("/reminders")
    public ResponseEntity<?> createReminder(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            String channelId = request.get("channelId");
            String title = request.get("title");
            String description = request.get("description");
            String remindAtStr = request.get("remindAt");
            
            java.time.LocalDateTime remindAt = java.time.LocalDateTime.parse(remindAtStr);
            ReminderApprovalService.Reminder reminder = reminderApprovalService
                .createReminder(userId, channelId, title, description, remindAt);
            
            return ResponseEntity.ok(reminder);
        } catch (Exception e) {
            logger.error("Error creating reminder", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating reminder");
        }
    }
    
    /**
     * Get user reminders
     */
    @GetMapping("/reminders/{userId}")
    public ResponseEntity<?> getUserReminders(@PathVariable String userId) {
        try {
            List<ReminderApprovalService.Reminder> reminders = reminderApprovalService.getUserReminders(userId);
            return ResponseEntity.ok(reminders);
        } catch (Exception e) {
            logger.error("Error retrieving reminders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving reminders");
        }
    }
    
    /**
     * Create approval request
     */
    @PostMapping("/approvals")
    public ResponseEntity<?> createApprovalRequest(@RequestBody Map<String, Object> request) {
        try {
            String requesterId = (String) request.get("requesterId");
            String channelId = (String) request.get("channelId");
            String title = (String) request.get("title");
            String description = (String) request.get("description");
            @SuppressWarnings("unchecked")
            List<String> approverIds = (List<String>) request.get("approverIds");
            
            ReminderApprovalService.ApprovalRequest approval = reminderApprovalService
                .createApprovalRequest(requesterId, channelId, title, description, approverIds);
            
            return ResponseEntity.ok(approval);
        } catch (Exception e) {
            logger.error("Error creating approval request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating approval request");
        }
    }
    
    /**
     * Approve a request
     */
    @PostMapping("/approvals/{requestId}/approve")
    public ResponseEntity<?> approveRequest(@PathVariable String requestId,
                                           @RequestBody Map<String, String> request) {
        try {
            String approverId = request.get("approverId");
            String comment = request.get("comment");
            
            reminderApprovalService.approveRequest(requestId, approverId, comment);
            return ResponseEntity.ok("Approval recorded");
        } catch (Exception e) {
            logger.error("Error approving request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error approving request");
        }
    }
    
    /**
     * Initiate voice call
     */
    @PostMapping("/voice-calls")
    public ResponseEntity<?> initiateVoiceCall(@RequestBody Map<String, String> request) {
        try {
            String initiatorId = request.get("initiatorId");
            String recipientId = request.get("recipientId");
            String callTypeStr = request.get("callType");
            String channelId = request.get("channelId");
            
            VoiceCallService.CallType callType = VoiceCallService.CallType.valueOf(callTypeStr);
            VoiceCallService.VoiceCall call = voiceCallService.initiateCall(
                initiatorId, recipientId, callType, channelId
            );
            
            return ResponseEntity.ok(call);
        } catch (Exception e) {
            logger.error("Error initiating voice call", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error initiating call");
        }
    }
    
    /**
     * Get active call info
     */
    @GetMapping("/voice-calls/{callId}")
    public ResponseEntity<?> getCallInfo(@PathVariable String callId) {
        try {
            VoiceCallService.VoiceCall call = voiceCallService.getCallInfo(callId);
            if (call == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(call);
        } catch (Exception e) {
            logger.error("Error retrieving call info", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving call");
        }
    }
    
    /**
     * End a call
     */
    @PostMapping("/voice-calls/{callId}/end")
    public ResponseEntity<?> endCall(@PathVariable String callId,
                                     @RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            voiceCallService.endCall(callId, userId);
            return ResponseEntity.ok("Call ended");
        } catch (Exception e) {
            logger.error("Error ending call", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error ending call");
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Chat Service is running");
    }
}
