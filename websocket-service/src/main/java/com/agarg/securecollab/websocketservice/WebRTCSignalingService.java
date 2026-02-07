package com.agarg.securecollab.websocketservice;

import org.springframework.stereotype.Service;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebRTC Signaling Service
 * Manages peer connections, SDP exchange, ICE candidates
 * Uses TURN/STUN servers for NAT traversal
 */
@Service
public class WebRTCSignalingService {

    private static final Logger logger = LoggerFactory.getLogger(WebRTCSignalingService.class);

    // In-memory peer connections: callId -> PeerConnection
    private final Map<String, PeerConnection> connections = new HashMap<>();

    private static final String STUN_SERVER = "stun:stun.l.google.com:19302";
    private static final String TURN_SERVER = "turn:coturn.example.com:3478";
    private static final String TURN_USERNAME = "user";
    private static final String TURN_PASSWORD = "password";

    public void createPeerConnection(String callId, String initiatorId, String recipientId) {
        PeerConnection pc = new PeerConnection(callId, initiatorId, recipientId);
        connections.put(callId, pc);
        logger.info("Created peer connection: {}", callId);
    }

    public void addIceCandidate(String callId, String userId, Map<String, Object> candidate) {
        PeerConnection pc = connections.get(callId);
        if (pc != null) {
            pc.addCandidate(userId, candidate);
            logger.debug("ICE candidate added to {}: {}", callId, candidate);
        }
    }

    public void setLocalDescription(String callId, String userId, Map<String, String> sdp) {
        PeerConnection pc = connections.get(callId);
        if (pc != null) {
            pc.setLocalDescription(userId, sdp);
            logger.info("Local SDP set for {}: type={}", callId, sdp.get("type"));
        }
    }

    public void setRemoteDescription(String callId, String userId, Map<String, String> sdp) {
        PeerConnection pc = connections.get(callId);
        if (pc != null) {
            pc.setRemoteDescription(userId, sdp);
            logger.info("Remote SDP set for {}: type={}", callId, sdp.get("type"));
        }
    }

    public Map<String, Object> getIceServersConfig() {
        return Map.of(
            "iceServers", List.of(
                Map.of("urls", STUN_SERVER),
                Map.of(
                    "urls", TURN_SERVER,
                    "username", TURN_USERNAME,
                    "credential", TURN_PASSWORD
                )
            )
        );
    }

    public void closePeerConnection(String callId) {
        PeerConnection pc = connections.remove(callId);
        if (pc != null) {
            logger.info("Closed peer connection: {}", callId);
        }
    }

    public static class PeerConnection {
        private String callId;
        private String initiatorId;
        private String recipientId;
        private Map<String, Map<String, String>> localSdp = new HashMap<>();
        private Map<String, Map<String, String>> remoteSdp = new HashMap<>();
        private Map<String, List<Map<String, Object>>> candidates = new HashMap<>();

        public PeerConnection(String callId, String initiatorId, String recipientId) {
            this.callId = callId;
            this.initiatorId = initiatorId;
            this.recipientId = recipientId;
            this.candidates.put(initiatorId, new ArrayList<>());
            this.candidates.put(recipientId, new ArrayList<>());
        }

        public void addCandidate(String userId, Map<String, Object> candidate) {
            candidates.computeIfAbsent(userId, k -> new ArrayList<>()).add(candidate);
        }

        public void setLocalDescription(String userId, Map<String, String> sdp) {
            localSdp.put(userId, sdp);
        }

        public void setRemoteDescription(String userId, Map<String, String> sdp) {
            remoteSdp.put(userId, sdp);
        }

        public String getCallId() { return callId; }
        public String getInitiatorId() { return initiatorId; }
        public String getRecipientId() { return recipientId; }
        public List<Map<String, Object>> getCandidates(String userId) { return candidates.get(userId); }
    }
}
