package com.agarg.securecollab.repository;

import com.agarg.securecollab.chatservice.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    // Added methods for GDPR operations
    List<MessageEntity> findByFromUserIdOrToUserId(String fromUserId, String toUserId);
    void deleteAllByFromUserId(String userId);
    long countByFromUserIdOrToUserId(String fromUserId, String toUserId);
}