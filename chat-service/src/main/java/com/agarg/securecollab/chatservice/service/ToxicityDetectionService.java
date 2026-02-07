package com.agarg.securecollab.chatservice.service;

import org.springframework.stereotype.Service;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI-based toxicity detection service
 * Detects harmful, abusive, and toxic content in messages
 * Author: Anurag Garg
 */
@Service
public class ToxicityDetectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(ToxicityDetectionService.class);
    
    // Toxic keywords database
    private static final Set<String> TOXIC_KEYWORDS = Set.of(
        "hate", "abuse", "harassment", "spam", "toxic", "insult",
        "offensive", "degrade", "discriminate", "violent", "threat"
    );
    
    private static final Map<String, Float> TOXICITY_THRESHOLDS = Map.of(
        "HIGH", 0.8f,
        "MEDIUM", 0.5f,
        "LOW", 0.3f
    );
    
    /**
     * Analyze message for toxicity
     * @param message the message to analyze
     * @return ToxicityAnalysis result
     */
    public ToxicityAnalysis analyzeToxicity(String message) {
        if (message == null || message.trim().isEmpty()) {
            return new ToxicityAnalysis(message, 0.0f, "SAFE", Collections.emptyList());
        }
        
        String lowercaseMsg = message.toLowerCase();
        List<String> detectedToxins = new ArrayList<>();
        float toxicityScore = calculateToxicityScore(lowercaseMsg, detectedToxins);
        String severity = classifySeverity(toxicityScore);
        
        logger.info("Toxicity Analysis - Message: {}, Score: {}, Severity: {}", 
                   message.substring(0, Math.min(50, message.length())), 
                   toxicityScore, severity);
        
        return new ToxicityAnalysis(message, toxicityScore, severity, detectedToxins);
    }
    
    private float calculateToxicityScore(String message, List<String> detectedToxins) {
        float score = 0.0f;
        int matches = 0;
        
        for (String toxic : TOXIC_KEYWORDS) {
            if (message.contains(toxic)) {
                detectedToxins.add(toxic);
                score += 0.15f;
                matches++;
            }
        }
        
        // Length check - very long messages might be spam
        if (message.length() > 500) {
            score += 0.05f;
        }
        
        // Excessive caps or special characters
        long allCapsCount = message.chars().filter(Character::isUpperCase).count();
        if (allCapsCount > message.length() * 0.7) {
            score += 0.1f;
        }
        
        // Pattern matching for patterns like !!!, ???, etc
        if (message.matches(".*([!?]){3,}.*")) {
            score += 0.1f;
        }
        
        return Math.min(score, 1.0f);
    }
    
    private String classifySeverity(float score) {
        if (score >= TOXICITY_THRESHOLDS.get("HIGH")) {
            return "HIGH";
        } else if (score >= TOXICITY_THRESHOLDS.get("MEDIUM")) {
            return "MEDIUM";
        } else if (score >= TOXICITY_THRESHOLDS.get("LOW")) {
            return "LOW";
        }
        return "SAFE";
    }
    
    public static class ToxicityAnalysis {
        public final String message;
        public final float toxicityScore;
        public final String severity;
        public final List<String> detectedToxins;
        
        public ToxicityAnalysis(String message, float toxicityScore, String severity, List<String> detectedToxins) {
            this.message = message;
            this.toxicityScore = toxicityScore;
            this.severity = severity;
            this.detectedToxins = detectedToxins;
        }
        
        public boolean isBlockable() {
            return severity.equals("HIGH");
        }
        
        public boolean requiresReview() {
            return severity.equals("MEDIUM") || severity.equals("HIGH");
        }
    }
}
