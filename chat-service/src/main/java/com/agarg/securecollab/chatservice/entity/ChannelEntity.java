package com.agarg.securecollab.chatservice.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "channels")
public class ChannelEntity {
    @Id
    @Column(name = "channel_id", nullable = false, unique = true)
    private String channelId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "type")
    private String type;

    @Column(name = "owner_id")
    private String ownerId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ChannelEntity() {}

    public ChannelEntity(String channelId, String name, String description, String type, String ownerId, LocalDateTime createdAt) {
        this.channelId = channelId;
        this.name = name;
        this.description = description;
        this.type = type;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
    }

    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
