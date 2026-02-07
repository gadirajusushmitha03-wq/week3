package com.agarg.securecollab.chatservice.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Voice Call Service
 * Handles real-time voice and video calling functionality
 * Author: Anurag Garg
 */
@Service
public class VoiceCallService {
    
    private static final Logger logger = LoggerFactory.getLogger(VoiceCallService.class);
    
    private final Map<String, VoiceCall> activeCalls = new HashMap<>();
    private final Map<String, CallInvite> invites = new HashMap<>();
    
    /**
     * Initiate a voice call
     */
    public VoiceCall initiateCall(String initiatorId, String recipientId, 
                                  CallType callType, String channelId) {
        
        VoiceCall call = new VoiceCall(initiatorId, recipientId, callType, channelId);
        activeCalls.put(call.getCallId(), call);
        
        logger.info("Voice call initiated: {} (Type: {}) between {} and {}", 
                   call.getCallId(), callType, initiatorId, recipientId);
        
        return call;
    }
    
    /**
     * Create a call invite
     */
    public CallInvite createCallInvite(String initiatorId, String recipientId, 
                                       CallType callType, String channelId) {
        
        CallInvite invite = new CallInvite(initiatorId, recipientId, callType, channelId);
        invites.put(invite.getInviteId(), invite);
        
        logger.info("Call invite created: {} for user: {}", invite.getInviteId(), recipientId);
        
        return invite;
    }
    
    /**
     * Accept a call invite
     */
    public VoiceCall acceptCallInvite(String inviteId, String recipientId) {
        
        CallInvite invite = invites.remove(inviteId);
        if (invite == null) {
            throw new IllegalArgumentException("Invite not found: " + inviteId);
        }
        
        if (!invite.getRecipientId().equals(recipientId)) {
            throw new SecurityException("User not authorized to accept this invite");
        }
        
        VoiceCall call = new VoiceCall(invite.getInitiatorId(), recipientId, 
                                       invite.getCallType(), invite.getChannelId());
        activeCalls.put(call.getCallId(), call);
        call.setStatus(VoiceCall.CallStatus.CONNECTED);
        call.setAcceptedAt(LocalDateTime.now());
        
        logger.info("Call invite accepted: {}", inviteId);
        
        return call;
    }
    
    /**
     * Reject a call invite
     */
    public void rejectCallInvite(String inviteId, String recipientId) {
        
        CallInvite invite = invites.remove(inviteId);
        if (invite == null) {
            throw new IllegalArgumentException("Invite not found: " + inviteId);
        }
        
        if (!invite.getRecipientId().equals(recipientId)) {
            throw new SecurityException("User not authorized to reject this invite");
        }
        
        logger.info("Call invite rejected: {}", inviteId);
    }
    
    /**
     * End a call
     */
    public void endCall(String callId, String userId) {
        
        VoiceCall call = activeCalls.get(callId);
        if (call == null) {
            throw new IllegalArgumentException("Call not found: " + callId);
        }
        
        if (!call.getInitiatorId().equals(userId) && !call.getRecipientId().equals(userId)) {
            throw new SecurityException("User not part of this call");
        }
        
        call.setStatus(VoiceCall.CallStatus.ENDED);
        call.setEndedAt(LocalDateTime.now());
        activeCalls.remove(callId);
        
        logger.info("Call ended: {} by user: {}", callId, userId);
    }
    
    /**
     * Add participant to group call
     */
    public void addParticipant(String callId, String userId) {
        
        VoiceCall call = activeCalls.get(callId);
        if (call == null) {
            throw new IllegalArgumentException("Call not found: " + callId);
        }
        
        if (call.getCallType() == CallType.ONE_TO_ONE) {
            throw new IllegalArgumentException("Cannot add participants to one-to-one call");
        }
        
        call.addParticipant(userId);
        logger.info("Participant added to call: {} user: {}", callId, userId);
    }
    
    /**
     * Remove participant from call
     */
    public void removeParticipant(String callId, String userId) {
        
        VoiceCall call = activeCalls.get(callId);
        if (call == null) {
            throw new IllegalArgumentException("Call not found: " + callId);
        }
        
        call.removeParticipant(userId);
        logger.info("Participant removed from call: {} user: {}", callId, userId);
    }
    
    /**
     * Get active call info
     */
    public VoiceCall getCallInfo(String callId) {
        return activeCalls.get(callId);
    }
    
    /**
     * Get pending invites for a user
     */
    public List<CallInvite> getPendingInvites(String userId) {
        return invites.values().stream()
            .filter(i -> i.getRecipientId().equals(userId) && !i.isExpired())
            .toList();
    }
    
    public enum CallType {
        ONE_TO_ONE, GROUP
    }
    
    public static class VoiceCall {
        private String callId;
        private String initiatorId;
        private String recipientId;
        private CallType callType;
        private String channelId;
        private CallStatus status;
        private LocalDateTime initiatedAt;
        private LocalDateTime acceptedAt;
        private LocalDateTime endedAt;
        private Set<String> participants;
        private String signalingServerUrl;
        private String iceServers;
        
        public enum CallStatus {
            INITIATING, RINGING, CONNECTED, ENDED, FAILED
        }
        
        public VoiceCall(String initiatorId, String recipientId, CallType callType, String channelId) {
            this.callId = UUID.randomUUID().toString();
            this.initiatorId = initiatorId;
            this.recipientId = recipientId;
            this.callType = callType;
            this.channelId = channelId;
            this.status = CallStatus.INITIATING;
            this.initiatedAt = LocalDateTime.now();
            this.participants = new HashSet<>();
            this.participants.add(initiatorId);
            this.participants.add(recipientId);
            this.signalingServerUrl = "wss://signaling.securecollab.local/ws";
            this.iceServers = "stun:stun.l.google.com:19302,stun:stun1.l.google.com:19302";
        }
        
        public void addParticipant(String userId) {
            participants.add(userId);
        }
        
        public void removeParticipant(String userId) {
            participants.remove(userId);
        }
        
        public long getDurationSeconds() {
            if (endedAt == null) {
                return java.time.temporal.ChronoUnit.SECONDS.between(initiatedAt, LocalDateTime.now());
            }
            return java.time.temporal.ChronoUnit.SECONDS.between(initiatedAt, endedAt);
        }
        
        // Getters
        public String getCallId() { return callId; }
        public String getInitiatorId() { return initiatorId; }
        public String getRecipientId() { return recipientId; }
        public CallType getCallType() { return callType; }
        public String getChannelId() { return channelId; }
        public CallStatus getStatus() { return status; }
        public void setStatus(CallStatus status) { this.status = status; }
        public LocalDateTime getInitiatedAt() { return initiatedAt; }
        public LocalDateTime getAcceptedAt() { return acceptedAt; }
        public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }
        public LocalDateTime getEndedAt() { return endedAt; }
        public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
        public Set<String> getParticipants() { return new HashSet<>(participants); }
        public String getSignalingServerUrl() { return signalingServerUrl; }
        public String getIceServers() { return iceServers; }
    }
    
    public static class CallInvite {
        private String inviteId;
        private String initiatorId;
        private String recipientId;
        private CallType callType;
        private String channelId;
        private LocalDateTime createdAt;
        private static final long INVITE_EXPIRY_MS = 60 * 1000; // 60 seconds
        
        public CallInvite(String initiatorId, String recipientId, CallType callType, String channelId) {
            this.inviteId = UUID.randomUUID().toString();
            this.initiatorId = initiatorId;
            this.recipientId = recipientId;
            this.callType = callType;
            this.channelId = channelId;
            this.createdAt = LocalDateTime.now();
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - createdAt.atZone(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli() > INVITE_EXPIRY_MS;
        }
        
        // Getters
        public String getInviteId() { return inviteId; }
        public String getInitiatorId() { return initiatorId; }
        public String getRecipientId() { return recipientId; }
        public CallType getCallType() { return callType; }
        public String getChannelId() { return channelId; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }
}
