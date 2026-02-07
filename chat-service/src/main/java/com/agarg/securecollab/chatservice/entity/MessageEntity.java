package com.agarg.securecollab.chatservice.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class MessageEntity {

    @Id
    @Column(name = "message_id", nullable = false, unique = true)
    private String messageId;

    @Column(name = "channel_id")
    private String channelId;

    @Column(name = "sender_id")
    private String senderId;

    @Lob
    @Column(name = "encrypted_content", nullable = false)
    private String encryptedContent;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public MessageEntity() {}

    public MessageEntity(String messageId, String channelId, String senderId, String encryptedContent, String status, LocalDateTime createdAt) {
        this.messageId = messageId;
        this.channelId = channelId;
        this.senderId = senderId;
        this.encryptedContent = encryptedContent;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getEncryptedContent() { return encryptedContent; }
    public void setEncryptedContent(String encryptedContent) { this.encryptedContent = encryptedContent; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
