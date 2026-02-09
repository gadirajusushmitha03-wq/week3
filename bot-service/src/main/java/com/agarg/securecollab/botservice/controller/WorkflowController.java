package com.agarg.securecollab.botservice.controller;

import com.agarg.securecollab.botservice.model.BotWorkflow;
import com.agarg.securecollab.botservice.service.WorkflowEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {
    
    @Autowired
    private WorkflowEngineService workflowEngineService;
    
    @PostMapping
    public ResponseEntity<BotWorkflow> createWorkflow(@RequestBody BotWorkflow workflow) {
        BotWorkflow saved = workflowEngineService.saveWorkflow(workflow);
        return ResponseEntity.ok(saved);
    }
    
    @GetMapping
    public ResponseEntity<List<BotWorkflow>> getAllWorkflows() {
        List<BotWorkflow> workflows = workflowEngineService.getAllEnabledWorkflows();
        return ResponseEntity.ok(workflows);
    }
    
    @PostMapping("/trigger/message")
    public ResponseEntity<Void> triggerMessageEvent(
            @RequestParam String messageId,
            @RequestParam String content,
            @RequestParam String channelId,
            @RequestParam String userId) {
        workflowEngineService.processMessageEvent(messageId, content, channelId, userId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/trigger/mention")
    public ResponseEntity<Void> triggerMentionEvent(
            @RequestParam String messageId,
            @RequestParam String content,
            @RequestParam String channelId,
            @RequestParam String userId,
            @RequestParam String botName) {
        workflowEngineService.processMentionEvent(messageId, content, channelId, userId, botName);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Bot service is running");
    }
}
