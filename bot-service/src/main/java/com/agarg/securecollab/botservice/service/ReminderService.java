package com.agarg.securecollab.botservice.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ReminderService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReminderService.class);
    
    @KafkaListener(topics = "reminders", groupId = "bot-service-group")
    public void handleReminder(String reminder) {
        try {
            logger.info("Processing reminder: {}", reminder);
            // Parse reminder JSON and send to user
            // TODO: Implement reminder delivery
        } catch (Exception e) {
            logger.error("Error processing reminder", e);
        }
    }
}
