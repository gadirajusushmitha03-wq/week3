package com.agarg.securecollab.chatservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "key_bundles")
public class KeyBundleEntity {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Lob
    @Column(name = "identity_key", nullable = false, columnDefinition = "text")
    private String identityKey; // public identity key (Base64)

    @Lob
    @Column(name = "pre_key", columnDefinition = "text")
    private String preKey; // one-time pre-key (Base64)

    @Lob
    @Column(name = "signed_pre_key", columnDefinition = "text")
    private String signedPreKey; // signed pre-key (Base64)

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "public_key")
    private String publicKey;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    public KeyBundleEntity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public KeyBundleEntity(String userId, String deviceId, String identityKey, String preKey, String signedPreKey) {
        this();
        this.userId = userId;
        this.deviceId = deviceId;
        this.identityKey = identityKey;
        this.preKey = preKey;
        this.signedPreKey = signedPreKey;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getIdentityKey() { return identityKey; }
    public void setIdentityKey(String identityKey) { this.identityKey = identityKey; }
    public String getPreKey() { return preKey; }
    public void setPreKey(String preKey) { this.preKey = preKey; }
    public String getSignedPreKey() { return signedPreKey; }
    public void setSignedPreKey(String signedPreKey) { this.signedPreKey = signedPreKey; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getPublicKey() {
        return publicKey;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }
}
