package com.agarg.securecollab.botservice.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ApprovalService {
    
    private static final Logger logger = LoggerFactory.getLogger(ApprovalService.class);
    
    @KafkaListener(topics = "approvals", groupId = "bot-service-group")
    public void handleApprovalRequest(String request) {
        try {
            logger.info("Processing approval request: {}", request);
            // Parse approval JSON and send to approvers
            // TODO: Implement approval workflow
        } catch (Exception e) {
            logger.error("Error processing approval request", e);
        }
    }
}
