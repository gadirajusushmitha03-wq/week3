package com.agarg.securecollab.websocketservice;

import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.Controller;

// DISABLED: Using EnhancedChatController instead
// @Controller
public class ChatController {

    @MessageMapping("/chat.send")
    @SendTo("/topic/messages")
    public String send(String encryptedPayload) {
        return encryptedPayload; // server never decrypts
    }
}
