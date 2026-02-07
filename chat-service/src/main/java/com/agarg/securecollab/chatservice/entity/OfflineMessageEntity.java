package com.agarg.securecollab.chatservice.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "offline_messages")
public class OfflineMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false, unique = true)
    private String messageId;

    @Column(name = "recipient_id", nullable = false)
    private String recipientId;

    @Column(name = "channel_id")
    private String channelId;

    @Column(name = "sender_id")
    private String senderId;

    @Lob
    @Column(name = "encrypted_payload", nullable = false)
    private String encryptedPayload;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public OfflineMessageEntity() {
    }

    public OfflineMessageEntity(String messageId, String recipientId, String channelId, String senderId, String encryptedPayload, LocalDateTime createdAt, LocalDateTime expiresAt, String status) {
        this.messageId = messageId;
        this.recipientId = recipientId;
        this.channelId = channelId;
        this.senderId = senderId;
        this.encryptedPayload = encryptedPayload;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getEncryptedPayload() { return encryptedPayload; }
    public void setEncryptedPayload(String encryptedPayload) { this.encryptedPayload = encryptedPayload; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
